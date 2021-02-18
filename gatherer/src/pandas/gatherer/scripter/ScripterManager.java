package pandas.gatherer.scripter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.gather.InstanceRepository;
import pandas.gather.InstanceService;
import pandas.gatherer.core.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ScripterManager implements AutoCloseable {

	private final Logger log = LoggerFactory.getLogger(ScripterManager.class);
	private final List<Request> scriptRequests = new ArrayList<>();
	private final Set<Long> instancesInProgress = new HashSet<>();
	private static final String ARCHIVE_INSTANCE_REQUEST = "archiveInstance";
	private static final String DELETE_INSTANCE_REQUEST = "deleteInstance";
	static final String GLOBAL_REPLACE_REQUEST = "globalReplacement";
	private final InstanceService instanceService;
	private final InstanceRepository instanceRepository;
	private boolean systemShutDown;

	public ScripterManager(Config config, InstanceService instanceService, InstanceRepository instanceRepository) {
		this.instanceService = instanceService;
		this.instanceRepository = instanceRepository;
		log.info("starting ScripterManager thread");
		new ScripterPool(this, config, this.instanceService, this.instanceRepository);
		log.info("ScripterPool setup");
	}


	public synchronized boolean systemShutDown() {
		return systemShutDown;
	}
	
	public synchronized void setSystemShutDown(boolean value) {
		log.info("### ScripterManager setSystemShutDown="+value);
		systemShutDown = value;
	}

	/**
	 * Return the next script request by first checking the direct action queue. If no requests are found return null.
	 */
	synchronized Request getNextScriptRequest() {
		// if we have a (direct action) request waiting in queue, let's process it.
		if (!scriptRequests.isEmpty()) {
			Request request = scriptRequests.remove(0);
			instancesInProgress.add(request.instanceId);
			return request;
		}

		return null;
	}

	/**
	 * The agent should notify by calling this method when it has completed processing an instance.
	 */
	public void instanceFinished(long instanceId) {
		log.info("Finished instance " +  instanceId + " removing from instancesInProgress.");
		instancesInProgress.remove(instanceId);
		log.info("instancesInProgress=" + instancesInProgress);
	}

	private synchronized String processInstance(String instanceId, String action, List<String> args) {
		switch (action) {
			case ARCHIVE_INSTANCE_REQUEST:
			case DELETE_INSTANCE_REQUEST:
				return "direction action archive/delete requests are now ignored in favour of db state change";
			case GLOBAL_REPLACE_REQUEST:
				break;
			default:
				return "unknown action";
		}

		// add archive request to queue
		scriptRequests.add(new Request(Long.parseLong(instanceId), action, args));
		this.notifyAll();
		log.info("processInstance(): should have added request to queue");

		return "instance archive request queued: " + instanceId + "/" + action;
	}
	
	/**
	 * Attempt to secure a path by forcing it to be relative without any parent directory links (..)
	 */
	private String cleanPath(String path) {
		if (path.startsWith("/")) {
			path = "." + path;
		} else {
			path = "./" + path;
		}
		while(path.matches(".*/\\.\\..*")) {
			path = path.replaceAll("\\.\\.", "");
		}
		return path;
	}
	
	public synchronized String runSearchReplace(String email, String instanceId, String directory, String filename, String find, String replaceWith, Boolean regexMode, Boolean recursive, Boolean reportMode) {
		// Generate a globrep argument string for the search/replace query
		List<String> args = new ArrayList<>();
		args.add(email);
		args.add("-t");
		args.add("-c");
		
		if (reportMode) {
			args.add("-n");
		} else {
			args.add("-F");
		}
		
		if (!recursive) {
			args.add("-R");
		}
		
		if (filename != null) {
			args.add("-f");
			args.add(filename);
		}
		
		if (directory != null) {
			args.add("-d");
			args.add(cleanPath(directory));
		}
		
		if (regexMode) {
			args.add("-S");
			args.add(find);
		} else {
			args.add("-p");
			args.add(find);
			args.add("-s");
			// Replace with a single empty string otherwise it will be sent as NULL
			if (replaceWith == null || replaceWith.equals("")) {
				args.add("");
			} else {
				args.add(replaceWith);
			}
		}
		
		// and add it to the script queue.
		processInstance(instanceId, GLOBAL_REPLACE_REQUEST, args);
		
		log.info("Generated globrep command-line:" + String.join(" ", args));
		return "global replacement queued.";
	}

	@Override
	public void close() {
		setSystemShutDown(true);
	}

	static class Request {

		final long instanceId;
		final String action;
		final List<String> args;

		Request(long instanceId, String action, List<String> args) {
			this.instanceId = instanceId;
			this.action = action;
			this.args = args;
		}
	}

}
