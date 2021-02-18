package pandas.gatherer.httrack;//

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pandas.gather.*;
import pandas.gatherer.core.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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
	private final Config config;
	private final HttrackConfig httrackConfig;
	private final Scripts scripts;
	private final WorkingArea workingArea;
	private final Set<HTTrackProcess> running = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private boolean shutdown = false;
	private final InstanceService instanceService;
	private final InstanceRepository instanceRepository;

	public HttrackGatherer(Config config, HttrackConfig httrackConfig, Scripts scripts, WorkingArea workingArea, InstanceService instanceService, InstanceRepository instanceRepository) {
		this.httrackConfig = httrackConfig;
		this.scripts = scripts;
		this.workingArea = workingArea;
		this.config = config;
		this.instanceService = instanceService;
		this.instanceRepository = instanceRepository;
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
		StringBuilder optionsArgsAndUrls = new StringBuilder(instance.getTitle().getGather().getGatherCommand());
        StringBuilder theCommand = new StringBuilder(httrackConfig.getExecutable() + " -qi ");

		// prepare the command
		// -O + output path quoted system path from property later
		Path instanceDir = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());
		theCommand.append(" -%H -O \"").append(instanceDir).append("/\" ");
		theCommand.append(optionsArgsAndUrls);
		String command = theCommand.toString();

		log.info("Executing {}", command);

		instance.setGatherCommand(command);
		instanceRepository.save(instance);

		File logFile = File.createTempFile("pandas-gatherer-" + instance.getHumanId(), ".log");
		try {
			HTTrackProcess httrack = new HTTrackProcess(instance, instanceDir, command, logFile);
			running.add(httrack);
			try {
				while (!httrack.process.waitFor(1, TimeUnit.SECONDS)) {
					instance = instanceService.refresh(instance);
					if (shutdown || !instance.getState().getName().equals(State.GATHERING)) {
						httrack.stop();
						httrack.waitKill(30);
						return;
					}
				}
			} finally {
				running.remove(httrack);
			}

			log.info("HTTrack {} returned {}", instance.getHumanId(), httrack.process.exitValue());
			log.info("Output: {}", new String(Files.readAllBytes(logFile.toPath()), UTF_8));
			if (httrack.process.exitValue() != 0) {
				String output = new String(Files.readAllBytes(logFile.toPath()), UTF_8);
				throw new GatherException("HTTrack returned error: " + output);
			}
		} finally {
			Files.deleteIfExists(logFile.toPath());
		}
	}

	public void postprocess(Instance instance) throws IOException, InterruptedException {
		scripts.archivePreserve(instance.getTitle().getPi(), instance.getDateString());
		scripts.postGather(instance.getTitle().getPi(), instance.getDateString());

		String tepUrl = arcUrlFromMap(instance.getTitle().getPi(), instance.getDateString());
		if (tepUrl != null) {
			instance.setTepUrl(tepUrl);
			instanceRepository.save(instance);
		}
	}

	@Override
	public void archive(Instance instance) throws IOException, InterruptedException {
		scripts.archiveMove(instance.getTitle().getPi(), instance.getDateString());
	}

	@Override
	public void delete(Instance instance) throws IOException {
		workingArea.deleteInstance(instance.getTitle().getPi(), instance.getDateString());
	}

	@Override
	public String version() {
		try {
			Process process = new ProcessBuilder(httrackConfig.getExecutable().toString(), "-#h")
					.redirectOutput(ProcessBuilder.Redirect.PIPE)
					.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8))) {
				return reader.readLine();
			} finally {
				process.destroyForcibly();
			}
		} catch (IOException e) {
			log.warn("Failed to get HTTrack version", e);
			return "HTTrack version unknown";
		}
	}

	@Override
	public void shutdown() {
		shutdown = true;
		for (HTTrackProcess httrack: running) {
			httrack.stop();
		}
		for (HTTrackProcess httrack: running) {
			httrack.waitKill(10);
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
			process = new ProcessBuilder("sh", "-c", "exec " + command)
					.directory(instanceDir.toFile())
					.redirectErrorStream(true)
					.redirectOutput(logFile)
					.start();
		}

		void stop() {
			// HTTrack shuts down cleanly with SIGINT, but that's tricky to send from Java
			Long pid = pid();
			if (pid != null) {
				log.info("Sending SIGINT to HTTrack {} (pid={})", instance.getHumanId(), pid);
				try {
					new ProcessBuilder("kill", "-INT", pid.toString()).inheritIO().start();
				} catch (Exception e) {
					log.warn("kill -INT " + pid + " failed", e);
					pid = null;
				}
			}

			if (pid == null) { // fallback if SIGINT failed
				// HTTrack's SIGTERM handling is buggy. Send two as it'll probably ignore the first.
				log.info("PID unavailable. Sending SIGTERM to HTTrack {}", instance.getHumanId());
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

		private Long pid() {
			// once we upgrade to Java 9+ we can use Process.pid() :-)
			try {
				Field field = process.getClass().getDeclaredField("pid");
				field.setAccessible(true);
				return field.getLong(process);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				return null;
			}
		}
	}

}

