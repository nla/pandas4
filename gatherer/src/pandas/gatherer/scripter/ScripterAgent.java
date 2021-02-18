package pandas.gatherer.scripter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.gather.InstanceService;
import pandas.gatherer.core.Config;
import pandas.gatherer.core.Scripts;

/**
*	ScriptAgent does not handle stopping or pausing or finishing of running scripts
 *	since Delete and Archive should not be stopped
*/
class ScripterAgent implements Runnable {

	private final Scripts scripts;
	private final ScripterManager scripterManager;
	private final Logger log = LoggerFactory.getLogger(ScripterAgent.class);
	private final InstanceService instanceService;
	private final InstanceRepository instanceRepository;
	private String selfName;

	public ScripterAgent(ScripterManager aScripterManager, Config config, InstanceService instanceService, InstanceRepository instanceRepository){
		this.scripts = new Scripts(config);
		scripterManager = aScripterManager;
		this.instanceService = instanceService;
		this.instanceRepository = instanceRepository;
		selfName = Thread.currentThread().getName();
	}
	
	public void run() {
		selfName = Thread.currentThread().getName();

		ScripterManager.Request scriptRequest;
		try {
			while (!scripterManager.systemShutDown()) {
				log.trace(selfName + " getting next script to run");

				scriptRequest = scripterManager.getNextScriptRequest();

				if (scriptRequest == null) {
					Thread.sleep(30000);
					continue;
				}
				
				long instanceId = scriptRequest.instanceId;
				log.info("instanceId: " + instanceId);
				
				try {
					Instance instance = instanceRepository.findById(instanceId).orElseThrow();
					if (instance != null) {

						log.info(selfName +  " starting script run");

						// update title instance state
						try {
							switch (scriptRequest.action) {
								case ScripterManager.GLOBAL_REPLACE_REQUEST:
									scripts.globalReplace(instance.getTitle().getPi(), instance.getDateString(), scriptRequest.args.toArray(new String[0]));
									break;
							}
						} catch (Exception e) {
							log.error("Script failed", e);
							instanceService.recordFailure(instance, "Script failed", e.getMessage(), "ScripterAgent");
						} finally {
							scripterManager.instanceFinished(instanceId);
						}
					}
				} catch (Exception e) {
					log.info("POTENTIAL EXCEPTION LOG in ScripterAgent instance");
					e.printStackTrace();
					log.info(selfName + " " + e + " in Scripter");
				}
			}
			
			log.info(selfName + " exiting thread.");
		} catch (Exception e) {
			log.info("POTENTIAL EXCEPTION LOG in ScripterAgent run");
			e.printStackTrace();
			log.info(selfName + " " + e + " in Scripter");
		}
	}

}
