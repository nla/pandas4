package pandas.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pandas.agency.Agency;
import pandas.agency.User;
import pandas.core.Organisation;
import pandas.core.Utils;
import pandas.gather.*;
import pandas.util.Strings;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TitleService {
    private static final Logger log = LoggerFactory.getLogger(TitleService.class);

    private final TitleRepository titleRepository;
    private final TitleGatherRepository titleGatherRepository;
    private final FormatRepository formatRepository;
    private final StatusRepository statusRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final OwnerHistoryRepository ownerHistoryRepository;
    private final GatherMethodRepository gatherMethodRepository;
    private final GatherScheduleRepository gatherScheduleRepository;
    private final PublisherRepository publisherRepository;
    private final ScopeRepository scopeRepository;
    private final OptionRepository optionRepository;

    public TitleService(FormatRepository formatRepository, StatusRepository statusRepository,
                        TitleRepository titleRepository, TitleGatherRepository titleGatherRepository,
                        StatusHistoryRepository statusHistoryRepository, OwnerHistoryRepository ownerHistoryRepository,
                        GatherMethodRepository gatherMethodRepository,
                        GatherScheduleRepository gatherScheduleRepository, PublisherRepository publisherRepository,
                        ScopeRepository scopeRepository, OptionRepository optionRepository) {
        this.titleRepository = titleRepository;
        this.titleGatherRepository = titleGatherRepository;
        this.formatRepository = formatRepository;
        this.statusRepository = statusRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.ownerHistoryRepository = ownerHistoryRepository;
        this.gatherMethodRepository = gatherMethodRepository;
        this.gatherScheduleRepository = gatherScheduleRepository;
        this.publisherRepository = publisherRepository;
        this.scopeRepository = scopeRepository;
        this.optionRepository = optionRepository;
    }

    @PreAuthorize("hasAuthority('PRIV_BULK_EDIT_TITLES')")
    public TitleBulkEditForm newBulkEditForm(List<Title> titles) {
        var form = new TitleBulkEditForm();
        form.setTitles(titles);
        form.setMethod(Utils.getIfSame(titles, t -> t.getGather() == null ? null : t.getGather().getMethod()));
        form.setProfile(Utils.getIfSame(titles, t -> t.getGather() == null ? null : t.getGather().getActiveProfile()));
        form.setSchedule(Utils.getIfSame(titles, t -> t.getGather() == null ? null : t.getGather().getSchedule()));
        form.setScope(Utils.getIfSame(titles, t -> t.getGather() == null ? null : t.getGather().getScope()));
        form.setAnbdNumber(Utils.getIfSame(titles, Title::getAnbdNumber));
        form.setOwner(Utils.getIfSame(titles, Title::getOwner));
        return form;
    }

    @PreAuthorize("hasPermission(null, 'Title', 'edit')")
    public TitleEditForm newTitleForm(List<Collection> collections, List<Subject> subjects) {
        TitleEditForm form = new TitleEditForm();
        form.setCollections(collections);
        form.setFormat(formatRepository.findById(Format.DEFAULT_ID).orElseThrow());
        form.setGatherMethod(gatherMethodRepository.findByName(GatherMethod.DEFAULT).orElseThrow());
        form.setGatherSchedule(gatherScheduleRepository.findByName(GatherSchedule.DEFAULT).orElseThrow());
        form.setScope(scopeRepository.findById(Scope.DEFAULT_ID).orElse(null));
        form.setStatus(statusRepository.findById(Status.SELECTED_ID).orElseThrow());
        form.setSubjects(subjects);
        form.setLegalDeposit(true);
        form.setCataloguingNotRequired(true);

        // prefill subjects based on the collections
        if ((subjects == null || subjects.isEmpty()) && (collections != null && !collections.isEmpty())) {
            Set<Subject> subjectList = new HashSet<>();
            for (Collection collection: collections) {
                subjectList.addAll(collection.getInheritedSubjects());
            }
            form.setSubjects(new ArrayList<>(subjectList));
        }

        return form;
    }

    @Transactional
    @PreAuthorize("hasPermission(#form.id, 'Title', 'edit')")
    public Title save(TitleEditForm form, User user) {
        Instant now = Instant.now();
        Title title = form.getId() == null ? new Title() : titleRepository.findById(form.getId()).orElseThrow();
        if (form.getId() == null) {
            title.setRegDate(now);
        }
        title.setAnbdNumber(Strings.emptyToNull(form.getAnbdNumber()));
        title.setCataloguingNotRequired(form.isCataloguingNotRequired());
        title.setCollections(form.getCollections());
        title.setDisappeared(form.isDisappeared());
        title.setFormat(form.getFormat() == null ? formatRepository.findById(Format.DEFAULT_ID).orElseThrow() : form.getFormat());
        title.setLegalDeposit(form.getLegalDeposit());
        title.setLocalDatabaseNo(Strings.emptyToNull(form.getLocalDatabaseNo()));
        title.setLocalReference(Strings.emptyToNull(form.getLocalReference()));
        title.setName(form.getName().trim());
        title.setShortDisplayName(title.getName().length() > 60 ? (title.getName().substring(0, 60) + "...") : title.getName());
        title.setNotes(Strings.emptyToNull(form.getNotes()));
        title.setSubjects(form.getSubjects());
        title.setTitleUrl(form.getTitleUrl());
        title.setUnableToArchive(form.isUnableToArchive());

        Tep tep = title.getTep(); // ensure we have a tep
        title.getTep().setDisplayTitle(title.getName());

        boolean statusChanged = false;
        if (!Objects.equals(title.getStatus(), form.getStatus())) {
            title.setStatus(form.getStatus());
            statusChanged = true;
        }
        if (title.getStatus() == null) {
            title.setStatus(statusRepository.findById(Status.NOMINATED_ID).orElseThrow());
            statusChanged = true;
        }

        if (form.getContinues() != null && title.getContinues().isEmpty()) {
            title.getContinues().add(new TitleHistory(form.getContinues(), title));
        }

        // if transitioning to the selected status, try to move to the appropriate permission status if possible
        if (statusChanged && title.getStatus().getId().equals(Status.SELECTED_ID) && title.getPermission() != null
            && title.getPermission().getState() != null) {
            Long newStatusId = switch (title.getPermission().getState().getName()) {
                case PermissionState.GRANTED -> Status.PERMISSION_GRANTED_ID;
                case PermissionState.DENIED -> Status.PERMISSION_DENIED_ID;
                case PermissionState.IMPOSSIBLE -> Status.PERMISSION_IMPOSSIBLE_ID;
                default -> null;
            };
            if (newStatusId != null) {
                title.setStatus(statusRepository.findById(newStatusId).orElseThrow());
            }
        }

        // set seed url
        String[] seeds = new String[0];
        if (Strings.isNullOrBlank(form.getSeedUrls())) {
            title.setSeedUrl(form.getTitleUrl());
        } else {
            seeds = form.getSeedUrls().split("\\s+");
            title.setSeedUrl(seeds[0]);
        }

        if (title.getId() == null) {
            // set initial owning user and agency
            if (user != null) {
                title.setOwner(user);
                title.setAgency(user.getRole().getOrganisation().getAgency());
            }
        }

        // create default permission
        if (title.getDefaultPermission() == null) {
            Permission permission = new Permission();
            permission.setStateName("Unknown");
            permission.setTypeName("Title Permission");
            permission.setBlanket(false);
            permission.setDescription("Pandas4 Default Title Permission");
            title.setDefaultPermission(permission);
            title.setPermission(permission);
        }

        // create or update publisher
        Publisher publisher = form.getPublisher();
        if (publisher == null && form.getPublisherName() != null) { // create new
            Organisation organisation = new Organisation();
            organisation.setAbn(form.getPublisherAbn());
            organisation.setName(form.getPublisherName().trim());
            organisation.setUrl(title.getTitleUrl());
            publisher = new Publisher();
            publisher.setOrganisation(organisation);
            publisher.setType(form.getPublisherType());
            publisher = publisherRepository.save(publisher);
        }
        title.setPublisher(publisher);

        titleRepository.save(title);

        // if there's no PI, populate it using the title id
        if (title.getPi() == null) {
            title.setPi(title.getId());
            titleRepository.save(title);
        }

        // create an owner history record if the title is new
        if (form.getId() == null && (title.getOwner() != null || title.getAgency() != null)) {
            OwnerHistory ownerHistory = new OwnerHistory();
            ownerHistory.setTitle(title);
            ownerHistory.setUser(title.getOwner());
            ownerHistory.setAgency(title.getAgency());
            ownerHistory.setNote("Created new title");
            ownerHistory.setDate(now);
            ownerHistoryRepository.save(ownerHistory);
        }

        // create a status history record if we changed it
        if (statusChanged) {
            recordStatusChange(title, user, now, form.getReason());
        }

        //
        // update gather dates
        //

        // create or update the corresponding TitleGather record
        TitleGather titleGather = title.getGather();
        if (titleGather == null) titleGather = createTitleGather(title);
        if (titleGather.getTitle() == null) {
            titleGather.setTitle(title);
        }
        titleGather.setActiveProfile(form.getActiveProfile()); // TODO: do we need to swap httrack config?
        titleGather.setGatherUrl(title.getSeedUrl() != null && !title.getSeedUrl().isBlank() ? title.getSeedUrl() : title.getTitleUrl());
        titleGather.setMethod(form.getGatherMethod());
        titleGather.setSchedule(form.getGatherSchedule());
        titleGather.setScope(form.getScope());
        if (seeds.length > 1) {
            titleGather.setAdditionalUrls(String.join(" ", Arrays.copyOfRange(seeds, 1, seeds.length)));
        } else {
            titleGather.setAdditionalUrls(null);
        }

        titleGather.setScheduledDate(form.getScheduledInstant());
        titleGather.replaceOneoffDates(form.getOneoffDates());

        titleGather.calculateNextGatherDate();
        titleGather.setGatherCommand(titleGather.buildHttrackCommand());

        String filters = form.getFilters() == null ? null : form.getFilters().replace('\n', ' ');
        var filtersArgument = titleGather.getFiltersOptionArgument();
        if (filtersArgument != null) {
            filtersArgument.setArgument(filters);
        } else if (filters != null && !filters.isBlank()) {
            filtersArgument = new OptionArgument();
            filtersArgument.setOption(optionRepository.findById(Option.GATHER_FILTERS_ID)
                    .orElseThrow(() -> new IllegalStateException("Couldn't find Option with id " +
                            Option.GATHER_FILTERS_ID + " (Gather Filters)")));
            titleGather.addOptionArgument(filtersArgument);
            filtersArgument.setArgument(filters);
        }

        titleGatherRepository.save(titleGather);

        // create legacy tep relation if needed
        if (title.getLegacyTepRelation() == null) {
            title.setLegacyTepRelation(title.getTep());
            titleRepository.save(title);
        }

        return title;
    }

    private TitleGather createTitleGather(Title title) {
        TitleGather gather = new TitleGather();
        gather.setTitle(title);
        gather.setMethod(gatherMethodRepository.findByName(GatherMethod.DEFAULT).orElseThrow());
        gather.setSchedule(gatherScheduleRepository.findByName(GatherSchedule.DEFAULT).orElseThrow());
        title.setGather(gather);
        return gather;
    }

    /**
     * Create a new status history record for this title. Assumes the status has already been updated.
     */
    private void recordStatusChange(Title title, User user, Instant now, Reason reason) {
        // ensure reason is actually applicable for this status
        if (reason != null && !reason.getStatus().equals(title.getStatus())) {
            log.warn("Tried to set inapplicable reason {} for status {}", reason.getName(), title.getStatus().getName());
            reason = null;
        }

        statusHistoryRepository.markPreviousEnd(title, now);
        var statusHistory = new StatusHistory();
        statusHistory.setStartDate(now);
        statusHistory.setStatus(title.getStatus());
        statusHistory.setReason(reason);
        statusHistory.setUser(user);
        statusHistory.setTitle(title);
        statusHistoryRepository.save(statusHistory);
    }

    private List<Title> findAllByIdInBatches(List<Long> titleIds) {
        int batchSize = 500;
        List<Title> titles = new ArrayList<>(titleIds.size());
        for (int i = 0; i < titleIds.size(); i += batchSize) {
            List<Long> batch = titleIds.subList(i, Math.min(titleIds.size(), i + batchSize));
            for (var title : titleRepository.findAllById(batch)) {
                titles.add(title);
            }
        }
        return titles;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PRIV_BULK_EDIT_TITLES')")
    public void bulkEdit(TitleBulkEditForm form, User currentUser) {
        log.info("Applying bulk change {}", form.toString());
        Instant now = Instant.now();
        var titles = findAllByIdInBatches(form.getTitles().stream().map(Title::getId).toList());
        for (Title title : titles) {
            if (form.isEditAnbdNumber()) title.setAnbdNumber(form.getAnbdNumber());

            if (form.isEditOwner()) {
                if (!Objects.equals(title.getOwner(), form.getOwner())) {
                    title.setOwner(form.getOwner());
                    OwnerHistory ownerHistory = new OwnerHistory();
                    ownerHistory.setTitle(title);
                    ownerHistory.setDate(now);
                    ownerHistory.setUser(form.getOwner());
                    ownerHistory.setAgency(title.getAgency());
                    ownerHistory.setTransferrer(currentUser);
                    ownerHistory.setNote("Bulk change");
                    ownerHistoryRepository.save(ownerHistory);
                }
            }

            if (form.isEditAddNote()) {
                title.setNotes(title.getNotes() == null ? form.getAddNote() : (title.getNotes() + "\n" + form.getAddNote()));
            }

            if (form.isEditSchedule() || form.isEditMethod() || form.isEditOneoffDate() || form.isEditScope()
                    || form.isEditProfile()) {
                TitleGather gather = title.getGather();
                if (gather == null) {
                    gather = createTitleGather(title);
                }
                if (form.isEditSchedule()) gather.setSchedule(form.getSchedule());
                if (form.isEditMethod()) gather.setMethod(form.getMethod());
                if (form.isEditProfile() && form.getProfile() != null
                        && form.getProfile().canBeAppliedTo(gather.getMethod())) {
                    gather.setActiveProfile(form.getProfile());
                }
                if (form.isEditScope()) gather.setScope(form.getScope());
                if (form.isEditOneoffDate()) {
                    Instant instant;
                    if (form.getOneoffDate() == null) {
                        instant = Instant.now();
                    } else {
                        instant = form.getOneoffDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
                    }
                    gather.addOneoffDate(instant);
                }
            }

            if (form.isEditStatus() && !title.getStatus().equals(form.getStatus()) &&
                    title.getStatus().isTransitionAllowed(form.getStatus())) {
                title.setStatus(form.getStatus());
                recordStatusChange(title, currentUser, Instant.now(), form.getReason());
            }

            if (!form.getCollectionsToAdd().isEmpty()) {
                Set<Long> existingCollectionIds = title.getCollections().stream().map(Collection::getId).collect(Collectors.toSet());
                for (Collection collection : form.getCollectionsToAdd()) {
                    if (!existingCollectionIds.contains(collection.getId())) {
                        title.addCollection(collection);
                    }
                }
            }

            if (!form.getSubjectsToAdd().isEmpty()) {
                Set<Long> existingSubjectIds = title.getSubjects().stream().map(Subject::getId).collect(Collectors.toSet());
                for (Subject subject : form.getSubjectsToAdd()) {
                    if (!existingSubjectIds.contains(subject.getId())) {
                        title.addSubject(subject);
                    }
                }
            }
        }
        titleRepository.saveAll(titles);
    }

    @PreAuthorize("hasPermission(#title, 'edit')")
    public TitleEditForm editForm(Title title) {
        return new TitleEditForm(title);
    }

    @Transactional
    public void transferOwnership(Title title, Agency newAgency, User newOwner, String note, User currentUser) {
        OwnerHistory ownerHistory = new OwnerHistory();
        ownerHistory.setTitle(title);
        ownerHistory.setDate(Instant.now());
        ownerHistory.setUser(newOwner);
        ownerHistory.setAgency(newAgency);
        ownerHistory.setTransferrer(currentUser);
        ownerHistory.setNote(note);
        title.setAgency(newAgency);
        title.setOwner(newOwner);
        title.getOwnerHistories().add(ownerHistory);
        titleRepository.save(title);
    }

    @NotNull
    public List<Status> allowedStatusTransitions(List<Title> titles) {
        List<Long> statusIds = titles.stream().map(Title::getStatus).distinct()
                .flatMap(status -> status.getAllowedTransitionIds().stream())
                .distinct().toList();
        var statusList = new ArrayList<Status>();
        statusRepository.findAllById(statusIds).forEach(statusList::add);
        statusList.sort(Comparator.comparing(Status::getId));
        return statusList;
    }
}
