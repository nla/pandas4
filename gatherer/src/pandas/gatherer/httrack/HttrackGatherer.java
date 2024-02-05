package pandas.gatherer.httrack;//

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pandas.gather.*;
import pandas.gatherer.core.Backend;
import pandas.gatherer.core.GatherException;
import pandas.gatherer.core.WorkingArea;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class HttrackGatherer implements Backend {
	private static final Logger log = LoggerFactory.getLogger(HttrackGatherer.class);
	private final HttrackConfig httrackConfig;
	private final WorkingArea workingArea;
	private final Set<HTTrackProcess> running = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private boolean shutdown = false;
	private final InstanceService instanceService;
	private final InstanceRepository instanceRepository;

	private static final Set<HttrackGatherer> gatherers = Collections.newSetFromMap(new ConcurrentHashMap<>());

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			for (HttrackGatherer gatherer: gatherers) {
				gatherer.shutdown();
			}
		}));
	}

	public HttrackGatherer(HttrackConfig httrackConfig, WorkingArea workingArea, InstanceService instanceService, InstanceRepository instanceRepository) {
		this.httrackConfig = httrackConfig;
		this.workingArea = workingArea;
		this.instanceService = instanceService;
		this.instanceRepository = instanceRepository;
		gatherers.add(this);
	}

	@Override
	public String getGatherMethod() {
		return GatherMethod.HTTRACK;
	}

	/**
	 * Perform the HTTrack gathering process.
	 */
	@Override
	public void gather(Instance instance) throws Exception {
		Path instanceDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());
		String command = instanceService.buildAndSaveHttrackCommand(instance.getId(),
				httrackConfig.getExecutable().toString(),
				instanceDir);
		log.info("Executing {}", command);
		if (!Files.exists(instanceDir)) Files.createDirectories(instanceDir);
		File logFile = File.createTempFile("pandas-gatherer-" + instance.getHumanId(), ".log");
		try {
			HTTrackProcess httrack = new HTTrackProcess(instance, instanceDir, command, logFile);
			running.add(httrack);

			// Set a deadline for the crawl to complete. If it doesn't complete by then, kill it.
			// Add 15 minutes to the crawl time limit to allow HTTrack to clean up after itself.
			// HTTrack sometimes hangs after hitting its own time limit.
			long deadline = System.currentTimeMillis() + instance.getTitle().getGather().getCrawlTimeLimitSeconds() * 1000L +
							15 * 60000L;
			try {
				while (!httrack.process.waitFor(1, TimeUnit.SECONDS)) {
					instance = instanceService.refresh(instance);
					if (shutdown || !instance.getState().getName().equals(State.GATHERING)
						|| System.currentTimeMillis() > deadline) {
						httrack.stop();
						httrack.waitKill(60);
						return;
					}
				}
			} finally {
				running.remove(httrack);
			}

			log.info("HTTrack {} returned {}", instance.getHumanId(), httrack.process.exitValue());
			log.info("Output: {}", Files.readString(logFile.toPath()));
			if (httrack.process.exitValue() != 0) {
				String output = Files.readString(logFile.toPath());
				throw new GatherException("HTTrack returned error: " + output);
			}
		} finally {
			Files.deleteIfExists(logFile.toPath());
		}
	}

	public void postprocess(Instance instance) throws IOException, InterruptedException {
		workingArea.preserveInstance(instance);
		Path root = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());
		HttrackUtils.postGather(instance.getTitle().getPi(), instance.getDateString(), root);

		String tepUrl = arcUrlFromMap(instance.getTitle().getPi(), instance.getDateString());
		if (tepUrl != null) {
			instance.setTepUrl(tepUrl);
			instanceRepository.save(instance);
		}
	}

	@Override
	public void archive(Instance instance) throws IOException, InterruptedException {
		workingArea.archiveInstance(instance);
	}

	@Override
	public void delete(Instance instance) throws IOException {
		workingArea.deleteInstance(instance.getTitle().getPi(), instance.getDateString());
	}

	@Override
	public String version() throws IOException {
		Process process = new ProcessBuilder(httrackConfig.getExecutable().toString(), "-#h")
				.redirectOutput(ProcessBuilder.Redirect.PIPE)
				.start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
			return reader.readLine();
		} finally {
			process.destroyForcibly();
		}
	}

	@Override
	public synchronized void shutdown() {
		if (!shutdown) {
			shutdown = true;
			for (HTTrackProcess httrack : running) {
				httrack.stop();
			}
			for (HTTrackProcess httrack : running) {
				httrack.waitKill(10);
			}
			gatherers.remove(this);
		}
	}

	@Override
	public int getWorkerCount() {
		return httrackConfig.getWorkers();
	}

	private String arcUrlFromMap(long pi, String dateString) {
		try (BufferedReader br = Files.newBufferedReader(workingArea.getInstanceDir(pi, dateString).resolve("url.map"))) {
			String line = br.readLine();
			if (line == null) {
				log.warn("url.map is blank");
				return null;
			}
			String[] parts = line.split("\\^\\^");
			if (parts.length < 2) {
				log.warn("first line of url.map is missing '^^'");
				return null;
			}
			return "/pan/" + parts[1];
		} catch (IOException e) {
			log.warn("unable to read url.map", e);
			return null;
		}
	}

	private static class HTTrackProcess {
		final Instance instance;
		final Process process;

		HTTrackProcess(Instance instance, Path instanceDir, String command, File logFile) throws IOException {
			this.instance = instance;
			process = new ProcessBuilder("/bin/sh", "-c", "exec " + command)
					.directory(instanceDir.toFile())
					.redirectErrorStream(true)
					.redirectOutput(logFile)
					.start();
		}

		void stop() {
			// HTTrack shuts down cleanly with SIGINT, but that's tricky to send from Java
			long pid = process.pid();
			log.info("Sending SIGINT to HTTrack {} (pid={})", instance.getHumanId(), pid);
			try {
				new ProcessBuilder("kill", "-INT", Long.toString(pid)).inheritIO().start();
			} catch (Exception e) {
				log.warn("kill -INT " + pid + " failed", e);

				// HTTrack's SIGTERM handling is buggy. Send two as it'll probably ignore the first.
				log.info("kill -INT failed. Sending SIGTERM to HTTrack {}", instance.getHumanId());
				process.destroy();
				process.destroy();
			}
		}

		void waitKill(int timeoutSeconds) {
			boolean exited;
			try {
				exited = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.warn("Interrupted waiting for HTTrack to exit.");
				exited = false;
			}

			if (!exited) {
				log.error("Timed out waiting for HTTrack {} to stop. Killing it.", instance.getHumanId());
				process.destroyForcibly();
			}

			log.info("HTTrack {} exited.", instance.getHumanId());
		}

	}

}

