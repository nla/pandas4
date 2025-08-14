package pandas.gatherer.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.gather.*;

import java.io.IOException;
import java.time.Instant;

class Worker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Worker.class);
	private final GatherManager gatherManager;
	private final WorkingArea workingArea;
	private final Backend backend;
	private final InstanceService instanceService;
	private final InstanceGatherRepository instanceGatherRepository;
	private final ThumbnailGenerator thumbnailGenerator;

	Worker(GatherManager gatherManager, InstanceService instanceService, InstanceGatherRepository instanceGatherRepository, WorkingArea workingArea, Backend backend, ThumbnailGenerator thumbnailGenerator) {
		this.gatherManager = gatherManager;
		this.instanceService = instanceService;
		this.workingArea = workingArea;
		this.backend = backend;
		this.instanceGatherRepository = instanceGatherRepository;
		this.thumbnailGenerator = thumbnailGenerator;
	}

	/**
	 * Main loop of the gatherer. Pick up a new title to be gathered from the GatherManager and start
	 * gathering it. If there are no titles currently available, wait a minute or so and try again.
	 */
	public void run() {
		long pollDelay = 1000;

		try {
			while (!gatherManager.isShutdown()) {

				Instance instance = gatherManager.nextInstance(backend.getGatherMethod(), Thread.currentThread().getName());
				if (instance == null) {
					Thread.sleep(pollDelay);
					continue;
				}

				try {
					processInstance(instance);
				} finally {
					gatherManager.gathererFinished(Thread.currentThread().getName());
				}
			}
		} catch (Exception e2) {
			log.info(Thread.currentThread().getName() + " " + e2 + " in Gatherer " + Thread.currentThread().getName());
			e2.printStackTrace();
		}
		log.debug("Shutdown");
	}
	
	/**
	 * Peform a gather operation.
	 *
	 * Gathering consists of 4 stages, intialisation, pre-processing, gathering,
	 * and post-processing. Not all gather types may implement all four stages.
	 */
	private void processInstance(Instance instance) {
		gatherManager.addGatherInstanceThreadWithId(Thread.currentThread().getName(), instance.getId());
		loop:
		while (!gatherManager.isShutdown()) {
			instance = instanceService.refresh(instance);
			log.info("{} {}", instance.getState(), instance.getHumanId());
			try {
				State nextState;
				switch (instance.getState()) {
					case CREATION:
						nameThread("C", instance);
						log.info("mkdir {}", workingArea.getInstanceDir(instance.getTitle().getPi(), instance.getDateString()));
						workingArea.createInstance(instance.getTitle().getPi(), instance.getDateString());
						if (backend.getGatherMethod().equals(GatherMethod.UPLOAD)) {
							// upload instances are special and stay in the creation state
							// not ideal but I don't want to rework the design now.
							break loop;
						}
						nextState = State.GATHERING;
						break;
					case GATHERING:
						nameThread("G", instance);
						Instant startTime = Instant.now();
						thumbnailGenerator.generateLiveThumbnail(instance);
						backend.gather(instance);
						nextState = State.GATHER_PROCESS;
						saveGatherStatistics(instance, startTime);
						break;
					case GATHER_PROCESS:
						nameThread("P", instance);
						backend.postprocess(instance);
						nextState = State.GATHERED;
						break;
					case ARCHIVING:
						nameThread("A", instance);
                        backend.archive(instance);
                        instanceService.publishInstanceImmediatelyIfNecessary(instance.getId());
                        nextState = State.ARCHIVED;
                        break;
					case DELETING:
						nameThread("D", instance);
                        backend.delete(instance);
                        nextState = State.DELETED;
                        break;
					default:
						break loop; // nothing more to do
				}

				/*
				 * Only move to the state if its unchanged. It might have been updated in the background
				 * by a user pausing or stopping an instance.
				 */
				if (instance.getState().equals(instanceService.refresh(instance).getState()) && !gatherManager.isShutdown()) {
					instanceService.updateState(instance.getId(), nextState);
				}
			} catch (Exception e) {
				log.error("{} {}", instance.getState(), instance.getHumanId(), e);
				// we try try hard not to crash as the worker will be permanently dead if we do
				// there might be an intermittent problem with the database so these
				// could fail.
				try {
					instanceService.recordFailure(instance.getId(), "Failed " + instance.getState(), e.getMessage(), Thread.currentThread().getName());
				} catch (Exception e2) {
					log.error("Error logging exception to db", e2);
				}
			} finally {
				clearThreadName();
			}
		}
	}

	private void clearThreadName() {
		Thread thread = Thread.currentThread();
		String name = thread.getName();
		int i = name.indexOf(" ");
		if (i >= 0) {
			thread.setName(name.substring(0, i));
		}
	}

	private void nameThread(String stateCode, Instance instance) {
		Thread thread = Thread.currentThread();
		thread.setName(thread.getName() + " " + stateCode + instance.getTitle().getPi());
	}

	private void saveGatherStatistics(Instance instance, Instant startTime) {
		try {
			instanceService.finishGather(instance.getId(), startTime);
			if (!instance.isHeritrixMethod()) {
				FileStats stats = workingArea.instanceStats(instance.getTitle().getPi(), instance.getDateString(), gatherManager::isShutdown);
				if (gatherManager.isShutdown()) {
					return;
				}
				instanceService.updateGatherStats(instance.getId(), stats.fileCount(), stats.size());
			}
		} catch (IOException e) {
			log.warn("Unable to save gather stats for " + instance.getHumanId(), e);
		}
	}

}
