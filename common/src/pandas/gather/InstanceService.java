package pandas.gather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pandas.agency.User;
import pandas.collection.Title;
import pandas.collection.TitleRepository;
import pandas.util.Strings;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static pandas.gather.State.FAILED;

@Service
public class InstanceService {
    private static final Logger log = LoggerFactory.getLogger(InstanceService.class);
    private final InstanceRepository instanceRepository;
    private final TitleRepository titleRepository;
    private final StateHistoryRepository stateHistoryReposistory;
    private final InstanceGatherRepository instanceGatherRepository;
    private final InstanceResourceRepository instanceResourceRepository;
    private final PandasExceptionLogRepository pandasExceptionLogRepository;

    public InstanceService(InstanceRepository instanceRepository, TitleRepository titleRepository, StateHistoryRepository stateHistoryReposistory, InstanceGatherRepository instanceGatherRepository, InstanceResourceRepository instanceResourceRepository, PandasExceptionLogRepository pandasExceptionLogRepository) {
        this.instanceRepository = instanceRepository;
        this.titleRepository = titleRepository;
        this.stateHistoryReposistory = stateHistoryReposistory;
        this.instanceGatherRepository = instanceGatherRepository;
        this.instanceResourceRepository = instanceResourceRepository;
        this.pandasExceptionLogRepository = pandasExceptionLogRepository;
    }

    @Transactional
    public Instance createInstance(String gatherMethod, Title title) {
        title = titleRepository.findById(title.getId()).orElseThrow();
        Instant now = Instant.now();

        Instance instance = new Instance(title, now, gatherMethod);
        instanceRepository.save(instance);

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

    @Transactional
    public void updateState(long instanceId, State state) {
        updateState(instanceId, state, null);
    }

    @Transactional
    public void updateState(long instanceId, State state, User user) {
        Instant now = Instant.now();
        Instance instance = instanceRepository.getOrThrow(instanceId);
        instance.changeState(state, user, now);
        instanceRepository.save(instance);
    }

    public Instance refresh(Instance instance) {
        return instanceRepository.findById(instance.getId()).orElseThrow();
    }

    /**
     * Log an gather exception and set the instance to failed.
     */
    @Transactional
    public void recordFailure(long instanceId, String summary, String message, String originator, Integer exitStatus) {
        Instant now = Instant.now();
        Instance instance = instanceRepository.getOrThrow(instanceId);
        pandasExceptionLogRepository.save(new PandasExceptionLog(now, instance, summary, message, originator,
                instance.getTitle().getPi(), 0L));
        instance.changeState(State.FAILED, null, now);
        instanceRepository.save(instance);
        if (exitStatus != null) {
            instance.getGather().setExitStatus(exitStatus);
            instanceGatherRepository.save(instance.getGather());
        }
    }

    @Transactional
    public void publishInstanceImmediatelyIfNecessary(long instanceId) {
        Instance instance = instanceRepository.getOrThrow(instanceId);
        if (instance.getTitle().getTep() != null && instance.getTitle().getTep().isPublishImmediately()) {
            instance.setIsDisplayed(true);
            instanceRepository.save(instance);
        }
    }

    @Transactional
    public void updateGatherStats(long instanceId, long fileCount, long size) {
        InstanceGather gather = instanceGatherRepository.findById(instanceId).orElseThrow();
        gather.setFiles(fileCount);
        gather.setSize(size);
        instanceGatherRepository.save(gather);
    }

    @PreAuthorize("hasPermission(#instance.title, 'edit')")
    @Transactional
    public void archive(Instance instance, User user) {
        instance = instanceRepository.findById(instance.getId()).orElseThrow(() -> new IllegalStateException("instance doesn't exist"));
        if (instance.getState().isArchivedOrArchiving()) return;
        if (!instance.canArchive()) throw new IllegalStateException("can't archive instance in state " + instance.getState());

        instance.changeState(State.ARCHIVING, user, Instant.now());

        // display the new instance immediately
        Title title = instance.getTitle();
        instance.setIsDisplayed(true);

        instanceRepository.save(instance);

        // create a TEP too if needed
        if (title.getLegacyTepRelation() == null) {
            title.setLegacyTepRelation(title.getTep());
            titleRepository.save(title);
        }
    }

    @Transactional
    public void finishGather(long instanceId, Instant startTime) {
        finishGather(instanceId, startTime, null);
    }

    @Transactional
    public void finishGather(long instanceId, Instant startTime, Integer exitStatus) {
        Instant now = Instant.now();
        InstanceGather insGather = instanceGatherRepository.findById(instanceId).orElseThrow();
        insGather.setStart(startTime);
        insGather.setTime(Duration.between(startTime, now).getSeconds() / 60);
        insGather.setFinish(now);
        if (exitStatus != null) {
            insGather.setExitStatus(exitStatus);
        }
        instanceGatherRepository.save(insGather);
    }

    @Transactional
    public void updateStartTime(Long instanceId, Instant startTime) {
        InstanceGather insGather = instanceGatherRepository.findById(instanceId).orElseThrow();
        insGather.setStart(startTime);
        instanceGatherRepository.save(insGather);
    }

    @PreAuthorize("hasPermission(#instanceId, 'Instance', 'edit')")
    @Transactional
    public void delete(long instanceId, User currentUser) {
        var instance = instanceRepository.getOrThrow(instanceId);
        instance.delete(currentUser, Instant.now());
        instanceRepository.save(instance);
    }

    @Transactional
    public void retryAfterFailure(long instanceId, User currentUser) {
        Instance instance = instanceRepository.getOrThrow(instanceId);
        instance.retryAfterFailure(currentUser, Instant.now());
        instanceRepository.save(instance);
    }

    @Transactional
    public void retryAllFailed(User currentUser) {
        var instances = instanceRepository.findByStateInOrderByDate(List.of(FAILED));
        Instant now = Instant.now();
        for (Instance instance : instances) {
            instance.retryAfterFailure(currentUser, now);
        }
        instanceRepository.saveAll(instances);
    }

    @Transactional
    public void saveQualityInfo(long instanceId, List<GatherIndicator> qualityScores) throws IOException {
        Instance instance = instanceRepository.getOrThrow(instanceId);
        instance.getGather().setIndicators(qualityScores);
        instanceRepository.save(instance);
    }

    @Transactional
    public void deleteAllFailed(User currentUser) {
        var instances = instanceRepository.findByStateInOrderByDate(List.of(FAILED));
        Instant now = Instant.now();
        for (Instance instance : instances) {
            instance.delete(currentUser, now);
        }
        instanceRepository.saveAll(instances);
    }

    @Transactional
    public List<String> buildAndSaveHttrackCommand(long instanceId, String executable, Path instanceDir) {
        Instance instance = instanceRepository.getOrThrow(instanceId);
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("-qi");
        command.add("-%H");
        command.add("-O");
        command.add(instanceDir.toString());

        command.addAll(instance.getTitle().getGather().buildHttrackCommand());

        instance.setGatherCommand(Strings.shellEncode(command));
        instanceRepository.save(instance);
        return command;
    }

    @Transactional
    public void updateSeedStatuses(long instanceId, List<InstanceSeed> seeds) {
        Instance instance = instanceRepository.getOrThrow(instanceId);
        instance.setSeeds(seeds);
        instanceRepository.save(instance);
    }

    @Transactional
    public void postProcess(long instanceId, User currentUser) {
        Instance instance = instanceRepository.getOrThrow(instanceId);
        if (!instance.canPostProcess()) throw new IllegalStateException("can't post process instance in state " + instance.getState().getStateName());
        instance.changeState(State.GATHER_PROCESS, currentUser, Instant.now());
    }

    @Transactional
    public void stop(long instanceId, User user) {
        var instance = instanceRepository.getOrThrow(instanceId);
        if (instance.canStop()) {
            instance.changeState(State.GATHERED, user, Instant.now());
            instanceRepository.save(instance);
        }
    }
}
