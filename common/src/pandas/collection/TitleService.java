package pandas.collection;

import jakarta.validation.constraints.NotNull;
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

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

@Service
public class TitleService {
    private static final Logger log = LoggerFactory.getLogger(TitleService.class);

    private final TitleRepository titleRepository;
    private final TitleGatherRepository titleGatherRepository;
    private final FormatRepository formatRepository;
    private final StatusRepository statusRepository;
    private final OwnerHistoryRepository ownerHistoryRepository;
    private final GatherMethodRepository gatherMethodRepository;
    private final GatherScheduleRepository gatherScheduleRepository;
    private final ProfileRepository profileRepository;
    private final PublisherRepository publisherRepository;
    private final ScopeRepository scopeRepository;
    private final OptionRepository optionRepository;
    private final ContactPersonRepository contactPersonRepository;
    private final Statuses statuses;

    public TitleService(FormatRepository formatRepository, StatusRepository statusRepository,
                        TitleRepository titleRepository, TitleGatherRepository titleGatherRepository,
                        OwnerHistoryRepository ownerHistoryRepository,
                        GatherMethodRepository gatherMethodRepository,
                        GatherScheduleRepository gatherScheduleRepository, ProfileRepository profileRepository, PublisherRepository publisherRepository,
                        ScopeRepository scopeRepository, OptionRepository optionRepository, ContactPersonRepository contactPersonRepository, Statuses statuses) {
        this.titleRepository = titleRepository;
        this.titleGatherRepository = titleGatherRepository;
        this.formatRepository = formatRepository;
        this.statusRepository = statusRepository;
        this.ownerHistoryRepository = ownerHistoryRepository;
        this.gatherMethodRepository = gatherMethodRepository;
        this.gatherScheduleRepository = gatherScheduleRepository;
        this.profileRepository = profileRepository;
        this.publisherRepository = publisherRepository;
        this.scopeRepository = scopeRepository;
        this.optionRepository = optionRepository;
        this.contactPersonRepository = contactPersonRepository;
        this.statuses = statuses;
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
    public TitleEditForm newTitleForm(Set<Collection> collections, Set<Subject> subjects) {
        if (collections == null) collections = new HashSet<>();
        if (subjects == null) subjects = new HashSet<>();

        // prefill subjects based on the collections
        if (subjects.isEmpty() && !collections.isEmpty()) {
            subjects = new HashSet<>();
            for (Collection collection: collections) {
                subjects.addAll(collection.getInheritedSubjects());
            }
        }

        TitleEditForm form = new TitleEditForm();
        form.setCollections(collections);
        form.setFormat(formatRepository.findById(Format.DEFAULT_ID).orElseThrow());
        form.setGatherMethod(gatherMethodRepository.findByName(GatherMethod.DEFAULT).orElseThrow());
        form.setGatherSchedule(gatherScheduleRepository.findByName(GatherSchedule.DEFAULT).orElseThrow());
        form.setScope(scopeRepository.findById(Scope.DEFAULT_ID).orElse(null));
        form.setStatus(statuses.selected());
        form.setSubjects(subjects);
        form.setPermissionType(TitleEditForm.PermissionTypeRadio.LEGAL_DEPOSIT);
        form.setCataloguingNotRequired(true);
        form.setTitlePermission(new PermissionEditForm());
        form.setActiveProfile(profileRepository.findFirstByIsDefaultIsTrue());
        return form;
    }

    @Transactional
    @PreAuthorize("hasPermission(#form.id, 'Title', 'edit')")
    public Title save(TitleEditForm form, User user) {
        Instant now = Instant.now();
        Title title;
        if (form.getId() == null) {
            title = new Title(user, now);
        } else {
            title = titleRepository.findById(form.getId()).orElseThrow();
        }
        title.setAnbdNumber(Strings.emptyToNull(form.getAnbdNumber()));
        title.setCataloguingNotRequired(form.isCataloguingNotRequired());
        title.setCollections(form.getCollections());
        title.setContinuedByTitles(form.getContinuedByTitles());
        title.setContinuesTitles(form.getContinuesTitles());
        title.setDisappeared(form.isDisappeared());
        title.setFormat(form.getFormat() == null ? formatRepository.findById(Format.DEFAULT_ID).orElseThrow() : form.getFormat());
        title.setLegalDeposit(form.getPermissionType() == TitleEditForm.PermissionTypeRadio.LEGAL_DEPOSIT);
        title.setLocalDatabaseNo(Strings.emptyToNull(form.getLocalDatabaseNo()));
        title.setLocalReference(Strings.emptyToNull(form.getLocalReference()));
        title.setName(form.getName().trim());
        title.setShortDisplayName(title.getName().length() > 60 ? (title.getName().substring(0, 60) + "...") : title.getName());
        title.setNotes(Strings.emptyToNull(form.getNotes()));
        title.setSubjects(form.getSubjects());
        title.setUnableToArchive(form.isUnableToArchive());

        Tep tep = title.getTep(); // ensure we have a tep
        title.getTep().setDisplayTitle(title.getName());

        if (form.getStatus() == null) form.setStatus(statuses.nominated());
        title.changeStatus(form.getStatus(), form.getReason(), user, now);

        // Link as the next title in a series
        if (form.getContinues() != null) {
            title.setContinuedByTitles(Set.of());
            title.setContinuesTitles(Set.of(form.getContinues()));
        }

        // remove any deleted TitleHistory entries
        title.getContinuedBy().removeIf(th -> !form.getContinuedByTitles().contains(th.getContinues()));

        // now create any new TitleHistory entries

        for (Title continuedByTitle: form.getContinuedByTitles()) {
            if (title.getContinuedBy().stream().noneMatch(th -> th.getContinues().equals(continuedByTitle))) {
                title.getContinuedBy().add(new TitleHistory(title, continuedByTitle));
            }
        }

        // set seed url
        String[] seeds = form.getSeedUrls().split("\\s+");
        title.setSeedUrl(seeds[0]);
        if (form.getTitleUrl() == null || form.getTitleUrl().isBlank()) {
            title.setTitleUrl(seeds[0]);
        } else {
            title.setTitleUrl(form.getTitleUrl());
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

        // create default permission
        if (title.getDefaultPermission() == null) {
            Permission permission = new Permission(title);
            title.setDefaultPermission(permission);
            title.setPermission(permission);
        }

        if (form.getPermissionType() == TitleEditForm.PermissionTypeRadio.TITLE) {
            Permission permission = title.getDefaultPermission();
            PermissionEditForm permissionForm = form.getTitlePermission();
            permissionForm.applyTo(permission);

            // Permission: Granted by: New title contact person
            if (!permissionForm.getNewTitleContact().isNameBlank()) {
                var contactPerson = contactPersonRepository.save(permissionForm.getNewTitleContact().build());
                title.getContactPeople().add(contactPerson);
                permission.setContactPerson(contactPerson);
            }

            // Permission: Granted by: New publisher contact person
            if (!permissionForm.getNewPublisherContact().isNameBlank()) {
                var contactPerson = permissionForm.getNewPublisherContact().build();
                contactPerson.setPublisher(publisher);
                contactPerson = contactPersonRepository.save(contactPerson);
                permission.setContactPerson(contactPerson);
            }

            title.setPermission(permission);
        } else if (form.getPermissionType() == TitleEditForm.PermissionTypeRadio.PUBLISHER &&
                   form.getPublisherPermission() != null) {
            title.setPermission(form.getPublisherPermission());
        }

        title.syncStatusWithPermissionState(statuses, user);
        titleRepository.save(title);

        // if there's no PI, populate it using the title id
        if (title.getPi() == null) {
            title.setPi(title.getId());
            titleRepository.save(title);
        }

        saveGatherSettings(form, title, seeds);

        // create legacy tep relation if needed
        if (title.getLegacyTepRelation() == null) {
            title.setLegacyTepRelation(title.getTep());
            titleRepository.save(title);
        }

        return title;
    }

    private void saveGatherSettings(TitleEditForm form, Title title, String[] seeds) {
        // create or update the corresponding TitleGather record
        TitleGather titleGather = title.getGather();
        if (titleGather == null) titleGather = createTitleGather(title);
        if (titleGather.getTitle() == null) {
            titleGather.setTitle(title);
        }
        titleGather.setActiveProfile(form.getActiveProfile()); // TODO: do we need to swap httrack config?
        titleGather.setGatherUrl(title.getSeedUrl() != null && !title.getSeedUrl().isBlank() ? title.getSeedUrl() : title.getTitleUrl());
        titleGather.setIgnoreRobotsTxt(form.getIgnoreRobotsTxt());
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
        titleGather.setGatherCommand(titleGather.buildHttrackCommandString());

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
    }

    private TitleGather createTitleGather(Title title) {
        TitleGather gather = new TitleGather();
        gather.setTitle(title);
        gather.setMethod(gatherMethodRepository.findByName(GatherMethod.DEFAULT).orElseThrow());
        gather.setSchedule(gatherScheduleRepository.findByName(GatherSchedule.DEFAULT).orElseThrow());
        title.setGather(gather);
        return gather;
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
                    title.transferOwnership(title.getAgency(), form.getOwner(), "Bulk change", currentUser, now);
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
                title.changeStatus(form.getStatus(), form.getReason(), currentUser, Instant.now());
                title.syncStatusWithPermissionState(statuses, currentUser);
            }

            title.addCollections(form.getCollectionsToAdd());
            title.removeCollections(form.getCollectionsToRemove());
            title.addSubjects(form.getSubjectsToAdd());
            title.removeSubjects(form.getSubjectsToRemove());
        }
        titleRepository.saveAll(titles);
    }

    /**
     * Updates the status of the given title based on its associated permission state.
     */
    @Transactional
    public void syncStatusWithPermissionState(Title title, User user) {
        title.syncStatusWithPermissionState(statuses, user);
        titleRepository.save(title);
    }

    @PreAuthorize("hasPermission(#title, 'edit')")
    public TitleEditForm editForm(Title title) {
        return new TitleEditForm(title);
    }

    @Transactional
    public void transferOwnership(Title title, Agency newAgency, User newOwner, String note, User currentUser) {
        title.transferOwnership(newAgency, newOwner, note, currentUser, Instant.now());
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
