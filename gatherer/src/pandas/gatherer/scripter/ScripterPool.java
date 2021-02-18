package pandas.gatherer.scripter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pandas.gather.InstanceRepository;
import pandas.gather.InstanceService;
import pandas.gatherer.core.Config;


class ScripterPool {

	private static final Logger log = LoggerFactory.getLogger(ScripterPool.class);
	private final ScripterManager scripterManager;
	private int nextScripterAgentId = 0;
	private final Config config;
	private final InstanceService instanceService;
	private final InstanceRepository instanceRepository;

	public ScripterPool(ScripterManager aScripterManager, Config config, InstanceService instanceService, InstanceRepository instanceRepository){
		scripterManager = aScripterManager;
		this.config = config;
		this.instanceService = instanceService;
		this.instanceRepository = instanceRepository;
		log.info("ScipterPool has reference to ScripterManager");
		this.preparePool();
	}
	
	private void preparePool() {
		
		log.info("preparing the scripter pool");
		try {

			int count = config.getScriptWorkers();
			for (int i=0; i < count; i++) {
				this.startNewScripterThread();
			}			
		
		// get the required number of other gatherers when required
		
		} catch (Exception e) {
			log.info("POTENTIAL EXCEPTION LOG");
			System.err.println(e + " in Scripter Pool");
			e.printStackTrace();
		}
		
	}
	
	private synchronized void startNewScripterThread(){
		
		try {
			String threadName = "scripter" + nextScripterAgentId;
			ScripterAgent newScripterAgent = new ScripterAgent(scripterManager, config, instanceService, instanceRepository);
			Thread newThread = new Thread(newScripterAgent, threadName);
			newThread.setDaemon(false); // these threads should not finish even if the others have
			newThread.start();
			nextScripterAgentId++;
		} catch(Exception e) {
			log.info("POTENTIAL EXCEPTION LOG");
			e.printStackTrace();
		}

	}
}
