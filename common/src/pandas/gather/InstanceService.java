package pandas.gather;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pandas.collection.Title;
import pandas.collection.TitleRepository;
import pandas.core.Individual;

import java.time.Instant;

@Service
public class InstanceService {
    private final InstanceRepository instanceRepository;
    private final TitleRepository titleRepository;
    private final GatherDateRepository gatherDateRepository;
    private final StateRepository stateRepository;
    private final StateHistoryRepository stateHistoryReposistory;
    private final InstanceGatherRepository instanceGatherRepository;
    private final InstanceResourceRepository instanceResourceRepository;
    private final PandasExceptionLogRepository pandasExceptionLogRepository;
    private final InstanceThumbnailRepository instanceThumbnailRepository;

    public InstanceService(InstanceRepository instanceRepository, TitleRepository titleRepository, GatherDateRepository gatherDateRepository, StateRepository stateRepository, StateHistoryRepository stateHistoryReposistory, InstanceGatherRepository instanceGatherRepository, InstanceResourceRepository instanceResourceRepository, PandasExceptionLogRepository pandasExceptionLogRepository, InstanceThumbnailRepository instanceThumbnailRepository) {
        this.instanceRepository = instanceRepository;
        this.titleRepository = titleRepository;
        this.gatherDateRepository = gatherDateRepository;
        this.stateRepository = stateRepository;
        this.stateHistoryReposistory = stateHistoryReposistory;
        this.instanceGatherRepository = instanceGatherRepository;
        this.instanceResourceRepository = instanceResourceRepository;
        this.pandasExceptionLogRepository = pandasExceptionLogRepository;
        this.instanceThumbnailRepository = instanceThumbnailRepository;
    }

    @Transactional
    public Instance createInstance(String gatherMethod, Title title) {
        title = titleRepository.findById(title.getId()).orElseThrow();
        Instant now = Instant.now();

        Instance instance = new Instance();
        instance.setTitle(title);
        instance.setDate(now);
        String gatherUrl = title.getGather().getGatherUrl();
        instance.setTepUrl("/pan/" + title.getPi() + "/" + instance.getDateString() + "/" +  gatherUrl.replaceFirst("^https?://", ""));
        instance.setGatheredUrl(gatherUrl);
        instance.setGatherMethodName(gatherMethod);
        instance.setState(stateRepository.findByName(State.CREATION).orElseThrow());
        instance.setPrefix("PAN"); // unused?
        instance.setProcessable(1L); // unused?
        instance.setRemoveable(1L); // unused?
        instance.setRestrictable(1L); // unused?
        instance.setTransportable(1L); // unused?
        instanceRepository.save(instance);

        insertStateHistory(instance, instance.getState(), now, null);

        InstanceGather instanceGather = new InstanceGather();
        instanceGather.setInstance(instance);
        instanceGatherRepository.save(instanceGather);

        InstanceResource instanceResource = new InstanceResource();
        instanceResource.setInstance(instance);
        instanceResourceRepository.save(instanceResource);

        title.getGather().getOneoffDates().removeIf(d -> d.getDate().isBefore(now));
        title.getGather().setLastGatherDate(instance.getDate());
        title.getGather().calculateNextGatherDate();
        titleRepository.save(title);

        return instance;
    }

    private void insertStateHistory(Instance instance, State state, Instant now, Individual user) {
        StateHistory stateHistory = new StateHistory();
        stateHistory.setInstance(instance);
        stateHistory.setState(state);
        stateHistory.setStartDate(now);
        stateHistory.setIndividual(user);
        stateHistoryReposistory.save(stateHistory);
    }

    @Transactional
    public void updateState(Instance instance, String stateName) {
        updateState(instance, stateName, null);
    }

    @Transactional
    public void updateState(Instance instance, String stateName, Individual user) {
        Instant now = Instant.now();
        State state = stateRepository.findByName(stateName).orElseThrow();
        instance.setState(state);
        instanceRepository.updateState(instance.getId(), stateName);
        stateHistoryReposistory.markPreviousStateEnd(instance.getId(), now);
        insertStateHistory(instance, state, now, user);
    }

    public Instance refresh(Instance instance) {
        return instanceRepository.findById(instance.getId()).orElseThrow();
    }

    /**
     * Log an gather exception and set the instance to failed.
     */
    @Transactional
    public void recordFailure(Instance instance, String summary, String message, String originator) {
        PandasExceptionLog logEntry = new PandasExceptionLog();
        logEntry.setDate(Instant.now());
        logEntry.setInstance(instance);
        logEntry.setSummary(summary);
        logEntry.setDetail(message);
        logEntry.setOriginator(originator);
        logEntry.setPi(instance.getTitle().getPi());
        logEntry.setViewed(0L);
        pandasExceptionLogRepository.save(logEntry);
        
        updateState(instance, State.FAILED);
    }

    @Transactional
    public void publishInstanceImmediatelyIfNecessary(long instanceId) {
        Instance instance = instanceRepository.findById(instanceId).orElseThrow();
        if (instance.getTitle().getTep() != null && instance.getTitle().getTep().isPublishImmediately()) {
            instance.setIsDisplayed(1L);
            instanceRepository.save(instance);
        }
    }

    @Transactional
    public void updateGatherStats(Instance instance, long fileCount, long size) {
        InstanceGather gather = instance.getGather();
        gather.setFiles(fileCount);
        gather.setSize(size);
        instanceGatherRepository.save(gather);
    }

    @Transactional
    public void archive(Instance instance, Individual user, boolean publish) {
        if (!instance.canDelete()) throw new IllegalStateException("can't archive instance in state " + instance.getState().getName());
        updateState(instance, State.ARCHIVING, user);
        if (publish) {
            instance.setIsDisplayed(1L);
        }
        instanceRepository.save(instance);
    }
}
