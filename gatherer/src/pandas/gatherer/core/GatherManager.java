package pandas.gatherer.core;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import pandas.collection.Title;
import pandas.collection.TitleRepository;
import pandas.gather.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;

@Component
public class GatherManager implements AutoCloseable, SmartLifecycle {
	private static final Logger log = LoggerFactory.getLogger(GatherManager.class);
	private final Map<Long, String> currentlyGatheringTitles = new ConcurrentHashMap<>(); // pi -> thread name
	private final Map<Long, String> currentInstances = new ConcurrentHashMap<>(); // instance id -> thread name
	private final Map<String, String> currentlyGatheringHosts = new ConcurrentHashMap<>(); // site -> thread name
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final List<Backend> enabledBackends;
	private final Config config;
	private volatile boolean systemShutDown;
	private final List<Backend> backends = new ArrayList<>();
	private final List<Thread> workerThreads = new ArrayList<>();
	private final WorkingArea workingArea;
	private final InstanceRepository instanceRepository;
	private final TitleRepository titleRepository;
	private final InstanceService instanceService;
	private final InstanceGatherRepository instanceGatherRepository;
	private final ThumbnailGenerator thumbnailGenerator;
	private final Object pollingLock = new Object();
	private String version;
	private volatile boolean paused;
	private boolean running;
	private ScheduledFuture<?> updateGatherStatsTask;

	public GatherManager(Config config, WorkingArea workingArea, InstanceRepository instanceRepository,
						 TitleRepository titleRepository, InstanceService instanceService,
						 InstanceGatherRepository instanceGatherRepository, List<Backend> backends, ThumbnailGenerator thumbnailGenerator) {
		this.workingArea = workingArea;
		this.instanceRepository = instanceRepository;
		this.titleRepository = titleRepository;
		this.instanceService = instanceService;
		this.instanceGatherRepository = instanceGatherRepository;
		this.thumbnailGenerator = thumbnailGenerator;
		this.enabledBackends = backends;
		this.config = config;
	}

	@Override
	public synchronized void start() {
		running = true;
		this.updateGatherStatsTask = scheduler.scheduleWithFixedDelay(this::updateGatherStats, 10, config.getGatherStatsPollSeconds(), TimeUnit.SECONDS);
		for (Backend backend : enabledBackends) {
			startWorkers(backend, backend.getWorkerCount());
		}
	}

	@Override
	public synchronized void stop() {
		close();
		if (updateGatherStatsTask != null) {
			updateGatherStatsTask.cancel(true);
		}
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public void startWorkers(Backend backend, int count) {
	    log.info("Starting {} {} workers", count, backend.getGatherMethod());
		for (int i = 0; i < count; i++) {
			Worker worker = new Worker(this, instanceService, instanceGatherRepository, workingArea, backend, thumbnailGenerator);
			Thread thread = new Thread(worker, backend.getGatherMethod() + i);
			workerThreads.add(thread);
			thread.start();
		}
		backends.add(backend);
	}

	/**
	 * Query the database and select the next instance for gathering.
	 */
	Instance nextInstance(String gatherMethod, String threadName) {
		if (paused) {
			return null;
		}
		synchronized (pollingLock) {
			// first consider incomplete instances
			for (Instance instance : instanceRepository.findIncomplete(gatherMethod)) {
				String primarySeedHost = instance.getTitle().getPrimarySeedHost();
				if (primarySeedHost == null) {
					log.warn("Title.getPrimarySeedHost() returned null for instance {}", instance.getHumanId());
					primarySeedHost = "unknown";
				}

				if (currentlyGatheringTitles.containsKey(instance.getTitle().getId())) {
					continue;
				}

				if (instance.getState().isGatheringOrCreation()) {
					if (currentlyGatheringHosts.containsKey(primarySeedHost)) {
						continue;
					}
					currentlyGatheringHosts.put(primarySeedHost, threadName);
				}

				currentlyGatheringTitles.put(instance.getTitle().getId(), threadName);
				return instance;
			}

			// now look for titles scheduled for a new gather
			// XXX: we ignore any titles that were last gathered within the current minute
			//      this is to ensure that we don't generate an instance with the same datestring
			//      as a previous one.
			Instant startOfThisMinute = LocalDateTime.now().withSecond(0).atZone(ZoneId.systemDefault()).toInstant();
			for (Title title : titleRepository.fetchNewGathers(gatherMethod, Instant.now(), startOfThisMinute)) {
				String primarySeedHost = title.getPrimarySeedHost();
				String ipAddress = null;
				if (primarySeedHost == null) {
					log.warn("Title.getPrimarySeedHost() returned null for title {}", title.getHumanId());
					primarySeedHost = "unknown";
				} else {
                    try {
                        var address = InetAddress.getByName(primarySeedHost);
						ipAddress = address.getHostAddress();
                    } catch (UnknownHostException e) {
                        // oh well
                    }
                }
				if (!currentlyGatheringTitles.containsKey(title.getId()) &&
					!currentlyGatheringHosts.containsKey(primarySeedHost) &&
					(ipAddress == null || !currentlyGatheringHosts.containsKey(ipAddress))) {
					currentlyGatheringTitles.put(title.getId(), threadName);
					currentlyGatheringHosts.put(primarySeedHost, threadName);
					if (ipAddress != null) currentlyGatheringHosts.put(ipAddress, threadName);
					return instanceService.createInstance(gatherMethod, title);
				}
			}

			return null;
		}
	}

	/**
	 * Gatherers should notify the manager when they are finished by calling this method. The
	 * manager can then delegate another instance of this title.
	 */
	void gathererFinished(String threadName) {
		currentlyGatheringTitles.entrySet().removeIf(e -> threadName.equals(e.getValue()));
		currentlyGatheringHosts.entrySet().removeIf(e -> threadName.equals(e.getValue()));
		currentInstances.entrySet().removeIf(e -> threadName.equals(e.getValue()));
	}

	void addGatherInstanceThreadWithId(String threadName, long instanceId) {
		currentInstances.put(instanceId, threadName);
	}

	public boolean isShutdown() {
		return systemShutDown;
	}

	private void updateGatherStats() {
		for (long instanceId : currentInstances.keySet()) {
			if (systemShutDown) break;
			Instance instance = instanceRepository.findById(instanceId).orElseThrow();
			if (instance.getGatherMethodName().equals(GatherMethod.HTTRACK) ||
					instance.getGatherMethodName().equals(GatherMethod.BROWSERTRIX)) {
				try {
					FileStats stats = workingArea.instanceStats(instance.getTitle().getPi(), instance.getDateString(), this::isShutdown);
					log.info("{} gather stats files={} size={}", instance.getHumanId(), stats.fileCount(), stats.size());
					InstanceGather gather = instance.getGather();
					gather.setSize(stats.size());
					gather.setFiles(stats.fileCount());
					instanceGatherRepository.save(gather);
				} catch (Exception e) {
					log.warn("Unable to update gather statistics for " + instance.getHumanId(), e);
				}
			}
		}
	}

	/*
	** externally available GatherManager actions
	*/	

	/**
	 * Check whether the instance with the given id has filesystem data in it.
	 */
	public String isInstancePopulated(long id) {
		Optional<Instance> instanceOptional = instanceRepository.findById(id);
		if (instanceOptional.isEmpty()) {
			log.error("Instance with id " + id + " not found.");
			return "BadInstance";
		}
		Instance instance = instanceOptional.get();

		Path path = workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString());
		log.info("Checking population of instance " + id + " in directory: " + path);

		File dir = path.toFile();

		String[] list = dir.list();
		if (list == null) {
			log.error("Instance directory not found: " + path);
			return "DirectoryNotFound";
		}

		if (list.length == 0) {
			return "NoData";
		}

		return "Ok";
	}

	public String version() {
		if (version == null) {
		    StringBuilder sb = new StringBuilder();
		    for (Backend backend: backends) {
				sb.append(backend.getGatherMethod() + " ");
				try {
					sb.append(backend.version()).append("\n");
				} catch (IOException e) {
					log.warn("Error getting " + backend.getGatherMethod() + " version", e);
					sb.append("unknown\n");
				}
			}
            version = sb.toString();
		}
		return version;
	}

	@PreDestroy
	@Override
	public void close() {
		log.info("Shutting down");
		synchronized (this) {
			systemShutDown = true;
			scheduler.shutdown();
			for (Backend backend: backends) {
				backend.shutdown();
			}
		}
		for (Thread thread: workerThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// just exit
			}
		}
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isPaused() {
		return paused;
	}

	public Set<Long> getCurrentTitles() {
		return Collections.unmodifiableSet(currentlyGatheringTitles.keySet());
	}

	public Set<String> getCurrentHosts() {
		return Collections.unmodifiableSet(currentlyGatheringHosts.keySet());
	}

	public Set<Long> getCurrentInstances() {
		return Collections.unmodifiableSet(currentInstances.keySet());
	}
}

