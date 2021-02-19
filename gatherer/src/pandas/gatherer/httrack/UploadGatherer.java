package pandas.gatherer.httrack;//

import org.springframework.stereotype.Component;
import pandas.gather.GatherMethod;
import pandas.gather.Instance;
import pandas.gatherer.core.Backend;
import pandas.gatherer.core.Config;
import pandas.gatherer.core.Scripts;
import pandas.gatherer.core.WorkingArea;

import java.io.IOException;

@Component
public class UploadGatherer implements Backend {
	private final Scripts scripts;
	private final WorkingArea workingArea;
	private final Config config;

	public UploadGatherer(Config config, Scripts scripts, WorkingArea workingArea) {
		this.config = config;
		this.scripts = scripts;
		this.workingArea = workingArea;
	}

	/**
	 * Perform the Upload gathering process. This method is empty for upload gatherer.
	 */
	@Override
	public void gather(Instance instance) {
		
	}

	@Override
	public void postprocess(Instance instance) throws IOException, InterruptedException {
		workingArea.preserveInstance(instance.getTitle().getPi(), instance.getDateString());
		scripts.uploadProcess(instance.getTitle().getPi(), instance.getDateString());
	}

	@Override
	public void archive(Instance instance) throws IOException, InterruptedException {
		workingArea.archiveInstance(instance.getTitle().getPi(), instance.getDateString());
	}

	@Override
	public void delete(Instance instance) throws IOException {
		workingArea.deleteInstance(instance.getTitle().getPi(), instance.getDateString());
	}

	@Override
	public String version() {
		return "PANDAS Gatherer " + getClass().getPackage().getImplementationVersion();
	}

	@Override
	public int getWorkerCount() {
		return config.getUploadWorkers();
	}

	@Override
	public String getGatherMethod() {
		return GatherMethod.UPLOAD;
	}
}
