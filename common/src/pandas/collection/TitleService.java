package pandas.collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pandas.core.Individual;
import pandas.core.Utils;
import pandas.gather.GatherMethodRepository;
import pandas.gather.GatherScheduleRepository;
import pandas.gather.TitleGather;
import pandas.gather.TitleGatherRepository;

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

    public TitleService(FormatRepository formatRepository, StatusRepository statusRepository,
                        TitleRepository titleRepository, TitleGatherRepository titleGatherRepository,
                        StatusHistoryRepository statusHistoryRepository, OwnerHistoryRepository ownerHistoryRepository,
                        GatherMethodRepository gatherMethodRepository,
                        GatherScheduleRepository gatherScheduleRepository) {
        this.titleRepository = titleRepository;
        this.titleGatherRepository = titleGatherRepository;
        this.formatRepository = formatRepository;
        this.statusRepository = statusRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.ownerHistoryRepository = ownerHistoryRepository;
        this.gatherMethodRepository = gatherMethodRepository;
        this.gatherScheduleRepository = gatherScheduleRepository;
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
        form.setFormat(formatRepository.findById(Format.INTEGRATING_ID).orElseThrow());
        form.setGatherMethod(gatherMethodRepository.findByName("Heritrix").orElseThrow());
        form.setGatherSchedule(gatherScheduleRepository.findByName("Annual").orElseThrow());
        form.setSubjects(subjects);
        form.setStatus(statusRepository.findById(Status.SELECTED_ID).orElseThrow());
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

    private static String emptyToNull(String s) {
        return s != null && s.isEmpty() ? null : s;
    }

    @Transactional
    @PreAuthorize("hasPermission(#form.id, 'Title', 'edit')")
    public Title save(TitleEditForm form, Individual user) {
        Instant now = Instant.now();
        Title title = form.getId() == null ? new Title() : titleRepository.findById(form.getId()).orElseThrow();
        if (form.getId() == null) {
            title.setRegDate(now);
        }
        title.setAnbdNumber(emptyToNull(form.getAnbdNumber()));
        title.setCataloguingNotRequired(form.isCataloguingNotRequired());
        title.setCollections(form.getCollections());
        title.setFormat(form.getFormat());
        title.setLocalDatabaseNo(emptyToNull(form.getLocalDatabaseNo()));
        title.setLocalReference(emptyToNull(form.getLocalReference()));
        title.setName(form.getName());
        title.setShortDisplayName(form.getName().length() > 60 ? (form.getName().substring(0, 60) + "...") : form.getName());
        title.setNotes(form.getNotes());
        title.setSubjects(form.getSubjects());
        title.setTitleUrl(form.getTitleUrl());
        if (title.getSeedUrl() == null) {
            title.setSeedUrl(form.getTitleUrl());
        }
        boolean statusChanged = false;
        if (!Objects.equals(title.getStatus(), form.getStatus())) {
            title.setStatus(form.getStatus());
            statusChanged = true;
        }
        if (title.getStatus() == null) {
            title.setStatus(statusRepository.findById(Status.SELECTED_ID).orElseThrow());
            statusChanged = true;
        }
        String[] seeds = form.getSeedUrls() == null ? new String[0] : form.getSeedUrls().split("\n+");
        if (seeds.length > 0) {
            title.setSeedUrl(seeds[0]);
        } else {
            title.setSeedUrl(form.getTitleUrl());
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
        if (titleGather.getGatherUrl() == null) {
            titleGather.setGatherUrl(title.getSeedUrl() != null ? title.getSeedUrl() : title.getTitleUrl());
        }
        titleGather.setSchedule(form.getGatherSchedule());
        titleGather.setMethod(form.getGatherMethod());
        if (seeds.length > 1) {
            titleGather.setAdditionalUrls(String.join("\n", Arrays.copyOfRange(seeds, 1, seeds.length)));
        } else {
            titleGather.setAdditionalUrls(null);
        }

        titleGather.replaceOneoffDates(form.getOneoffDates());

        titleGather.calculateNextGatherDate();
        titleGather.setGatherCommand(titleGather.buildHttrackCommand());

        // FIXME: does it make sense to have both this and next_gather_date?
        titleGather.setScheduledDate(titleGather.getNextGatherDate());

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
}
