package pandas.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pandas.agency.Agency;
import pandas.core.Individual;
import pandas.core.Organisation;
import pandas.core.Utils;
import pandas.gather.*;
import pandas.util.Strings;

import java.time.Instant;
import java.util.*;

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

    public TitleService(FormatRepository formatRepository, StatusRepository statusRepository,
                        TitleRepository titleRepository, TitleGatherRepository titleGatherRepository,
                        StatusHistoryRepository statusHistoryRepository, OwnerHistoryRepository ownerHistoryRepository,
                        GatherMethodRepository gatherMethodRepository,
                        GatherScheduleRepository gatherScheduleRepository, PublisherRepository publisherRepository, ScopeRepository scopeRepository) {
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
    }

    @PreAuthorize("hasAuthority('PRIV_BULK_EDIT_TITLES')")
    public TitleBulkEditForm newBulkEditForm(List<Title> titles) {
        var form = new TitleBulkEditForm();
        form.setTitles(titles);
        form.setMethod(Utils.getIfSame(titles, t -> t.getGather() == null ? null : t.getGather().getMethod()));
        form.setSchedule(Utils.getIfSame(titles, t -> t.getGather() == null ? null : t.getGather().getSchedule()));
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
                subjectList.addAll(collection.getSubjects());
            }
            form.setSubjects(new ArrayList<>(subjectList));
        }

        return form;
    }

    @Transactional
    @PreAuthorize("hasPermission(#form.id, 'Title', 'edit')")
    public Title save(TitleEditForm form, Individual user) {
        Instant now = Instant.now();
        Title title = form.getId() == null ? new Title() : titleRepository.findById(form.getId()).orElseThrow();
        if (form.getId() == null) {
            title.setRegDate(now);
        }
        title.setAnbdNumber(Strings.emptyToNull(form.getAnbdNumber()));
        title.setCataloguingNotRequired(form.isCataloguingNotRequired());
        title.setCollections(form.getCollections());
        title.setFormat(form.getFormat() == null ? formatRepository.findById(Format.DEFAULT_ID).orElseThrow() : form.getFormat());
        title.setLegalDeposit(form.getLegalDeposit());
        title.setLocalDatabaseNo(Strings.emptyToNull(form.getLocalDatabaseNo()));
        title.setLocalReference(Strings.emptyToNull(form.getLocalReference()));
        title.setName(form.getName().trim());
        title.setShortDisplayName(title.getName().length() > 60 ? (title.getName().substring(0, 60) + "...") : title.getName());
        title.setNotes(Strings.emptyToNull(form.getNotes()));
        title.setSubjects(form.getSubjects());
        title.setTitleUrl(form.getTitleUrl());
        boolean statusChanged = false;
        if (!Objects.equals(title.getStatus(), form.getStatus())) {
            title.setStatus(form.getStatus());
            statusChanged = true;
        }
        if (title.getStatus() == null) {
            title.setStatus(statusRepository.findById(Status.NOMINATED_ID).orElseThrow());
            statusChanged = true;
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
            organisation.setName(form.getPublisherName().trim());
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
            ownerHistory.setIndividual(title.getOwner());
            ownerHistory.setAgency(title.getAgency());
            ownerHistory.setNote("Created new title");
            ownerHistory.setDate(now);
            ownerHistoryRepository.save(ownerHistory);
        }

        // create a status history record if we changed it
        if (statusChanged) {
            recordStatusChange(title, user, now);
        }

        //
        // update gather dates
        //

        // create or update the corresponding TitleGather record
        TitleGather titleGather = title.getGather() == null ? new TitleGather() : title.getGather();
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

        if (titleGather.getMethod() == null) {
            titleGather.setMethod(gatherMethodRepository.findByName(GatherMethod.DEFAULT).orElseThrow());
            titleGather.setSchedule(gatherScheduleRepository.findByName(GatherSchedule.DEFAULT).orElseThrow());
        }

        titleGather.replaceOneoffDates(form.getOneoffDates());

        titleGather.calculateNextGatherDate();
        titleGather.setGatherCommand(titleGather.buildHttrackCommand());

        titleGatherRepository.save(titleGather);

        return title;
    }

    /**
     * Create a new status history record for this title. Assumes the status has already been updated.
     */
    private void recordStatusChange(Title title, Individual user, Instant now) {
        statusHistoryRepository.markPreviousEnd(title, now);
        var statusHistory = new StatusHistory();
        statusHistory.setStartDate(now);
        statusHistory.setStatus(title.getStatus());
        statusHistory.setIndividual(user);
        statusHistory.setTitle(title);
        statusHistoryRepository.save(statusHistory);
    }

    @Transactional
    @PreAuthorize("hasAuthority('PRIV_BULK_EDIT_TITLES')")
    public void bulkEdit(TitleBulkEditForm form, Individual currentUser) {
        log.info("Applying bulk change {}", form.toString());
        Instant now = Instant.now();
        List<TitleGather> gathers = new ArrayList<>();
        for (Title title: form.getTitles()) {
            if (form.isEditAnbdNumber()) title.setAnbdNumber(form.getAnbdNumber());

            if (form.isEditOwner()) {
                if (!Objects.equals(title.getOwner(), form.getOwner())) {
                    title.setOwner(form.getOwner());
                    OwnerHistory ownerHistory = new OwnerHistory();
                    ownerHistory.setTitle(title);
                    ownerHistory.setDate(now);
                    ownerHistory.setIndividual(form.getOwner());
                    ownerHistory.setAgency(title.getAgency());
                    ownerHistory.setTransferrer(currentUser);
                    ownerHistory.setNote("Bulk change");
                    ownerHistoryRepository.save(ownerHistory);
                }
            }

            if (form.isEditAddNote()) {
                title.setNotes(title.getNotes() == null ? form.getAddNote() : (title.getNotes() + "\n" + form.getAddNote()));
            }

            if (form.isEditSchedule() || form.isEditMethod()) {
                TitleGather gather = title.getGather();
                if (gather == null) {
                    gather = new TitleGather();
                    gather.setTitle(title);
                }
                if (form.isEditSchedule()) gather.setSchedule(form.getSchedule());
                if (form.isEditMethod()) gather.setMethod(form.getMethod());
                gathers.add(gather);
            }
        }
        titleRepository.saveAll(form.getTitles());
        titleGatherRepository.saveAll(gathers);
    }

    @PreAuthorize("hasPermission(#title, 'edit')")
    public TitleEditForm editForm(Title title) {
        return new TitleEditForm(title);
    }

    @Transactional
    public void transferOwnership(Title title, Agency newAgency, Individual newOwner, String note, Individual currentUser) {
        OwnerHistory ownerHistory = new OwnerHistory();
        ownerHistory.setTitle(title);
        ownerHistory.setDate(Instant.now());
        ownerHistory.setIndividual(newOwner);
        ownerHistory.setAgency(newAgency);
        ownerHistory.setTransferrer(currentUser);
        ownerHistory.setNote(note);
        title.setAgency(newAgency);
        title.setOwner(newOwner);
        title.getOwnerHistories().add(ownerHistory);
        titleRepository.save(title);
    }
}
