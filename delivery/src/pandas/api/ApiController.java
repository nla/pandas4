package pandas.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.collection.Collection;
import pandas.collection.*;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.util.DateFormats;
import pandas.util.TimeFrame;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingLong;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Reimplementation of the PANDAS 3 delivery API consumed by trove-web-archive.
 */
@Controller
public class ApiController {
    private static final long SUBJECT_RANGE_START = 15000;
    private static final long SUBJECT_RANGE_END = 15999;
    private final static long TOP_COLLECTION_ID = 0;
    private final static String TOP_COLLECTION_NAME = "Archived Websites";

    // We can't use the default Jackson date format anymore as it now  contains a ':' in the timezone and existing
    // clients aren't expecting that.
    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final AgencyRepository agencyRepository;
    private final CollectionRepository collectionRepository;
    private final InstanceRepository instanceRepository;
    private final SubjectRepository subjectRepository;
    private final TitleRepository titleRepository;
    private final AccessChecker accessChecker;

    public ApiController(TitleRepository titleRepository, CollectionRepository collectionRepository, AgencyRepository agencyRepository, InstanceRepository instanceRepository, AccessChecker accessChecker, SubjectRepository subjectRepository) {
        this.agencyRepository = agencyRepository;
        this.collectionRepository = collectionRepository;
        this.instanceRepository = instanceRepository;
        this.subjectRepository = subjectRepository;
        this.titleRepository = titleRepository;
        this.accessChecker = accessChecker;
    }

    @GetMapping(value = "/api", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String help() {
        StringBuilder out = new StringBuilder();
        out.append("""
                PANDORA API
                ===========
                
                This API provides access to browse metadata for the PANDORA selective web archive. It is primarily
                intended for internal use and may change without warning.
                
                """);
        var types = new TreeSet<Class<?>>(Comparator.comparing(Class::getSimpleName));
        types.add(AgencyJson.class);
        types.add(BreadcrumbJson.class);
        types.add(LegacyCollectionJson.class);
        types.add(CollectionDetailsJson.class);
        types.add(TitleHistoryJson.class);
        types.add(InstanceJson.class);
        types.add(IssueJson.class);
        types.add(IssueGroupJson.class);
        types.add(TitleJson.class);
        types.add(TitleDetailsJson.class);

        out.append("Routes\n");
        out.append("------\n\n");
        for (var method : ApiController.class.getDeclaredMethods()) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping != null) {
                out.append(getMapping.value()[0]).append(" -> ").append(method.getReturnType().getSimpleName()).append("\n");
                if (method.getReturnType().getName().startsWith("pandas.")) {
                    types.add(method.getReturnType());
                }
            }
        }

        out.append("\nTypes\n");
        out.append("-----\n\n");

        for (var type : types) {
            out.append("class ").append(type.getSimpleName()).append(" {\n");
            for (var field : type.getFields()) {
                Type fieldType = field.getGenericType();
                StringBuilder fieldTypeString;
                if (fieldType instanceof ParameterizedType) {
                    Class<?> rawType = (Class<?>) ((ParameterizedType) fieldType).getRawType();
                    fieldTypeString = new StringBuilder(rawType.getSimpleName());
                    Type[] typeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
                    if (typeArguments.length > 0) {
                        fieldTypeString.append("<");
                        for (var typeArgument : typeArguments) {
                            if (!fieldTypeString.toString().endsWith("<")) fieldTypeString.append(",");
                            fieldTypeString.append(((Class<?>) typeArgument).getSimpleName());
                        }
                        fieldTypeString.append(">");
                    }
                } else if (fieldType instanceof Class) {
                    fieldTypeString = new StringBuilder(((Class<?>) fieldType).getSimpleName());
                } else {
                    fieldTypeString = new StringBuilder(fieldType.getTypeName());
                }
                out.append("    ").append(fieldTypeString).append(" ").append(field.getName()).append(";\n");
            }
            out.append("}\n\n");
        }

        return out.toString();
    }

    @GetMapping(value = "/api/tep/{pi}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public TitleDetailsJson tep(@PathVariable("pi") long pi) {
        Title title = titleRepository.findByPi(pi).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No such title"));
        if (!title.isDeliverable()) throw new ResponseStatusException(NOT_FOUND, "Title not deliverable");

        TitleDetailsJson titleDetailsJson = new TitleDetailsJson(title);

        var allInstancesAndIssues = new ArrayList<AccessChecker.Restrictable>();
        allInstancesAndIssues.addAll(titleDetailsJson.instances);
        titleDetailsJson.issueGroups.forEach(group -> allInstancesAndIssues.addAll(group.issues));
        try {
            accessChecker.checkAccessBulk(allInstancesAndIssues);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return titleDetailsJson;
    }

    @GetMapping(value = "/api/tep/max-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long tepMaxId() {
        return titleRepository.maxPi();
    }

    @GetMapping(value = "/api/collection", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<BreadcrumbJson> collectionList(@RequestParam(value = "startId", required = false) Long startId,
                                                @RequestParam(value = "endId", required = false) Long endId,
                                                @RequestParam(value = "limit", defaultValue = "100") int limit) {
        if (startId == null) startId = Long.MIN_VALUE;
        if (endId == null) endId = Long.MAX_VALUE;
        List<BreadcrumbJson> collections = new ArrayList<>();
        if (startId <= TOP_COLLECTION_ID && TOP_COLLECTION_ID <= endId) {
            collections.add(new BreadcrumbJson(TOP_COLLECTION_ID, TOP_COLLECTION_NAME));
        }
        for (var subject: subjectRepository.findAll()) {
            if (startId <= subject.getId() && subject.getId() <= endId) {
                collections.add(new BreadcrumbJson(subject));
            }
        }
        collectionRepository.findByIdBetween(startId, endId, Pageable.ofSize(limit))
                .stream().map(BreadcrumbJson::new).forEach(collections::add);
        collections.sort(comparingLong(c -> c.id));
        return collections.subList(0, Integer.min(collections.size(), limit));
    }

    @GetMapping(value = "/api/collection/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CollectionDetailsJson collection(@PathVariable("id") long id) {
        if (id == TOP_COLLECTION_ID) {
            return topCollection();
        } else if (id >= SUBJECT_RANGE_START && id <= SUBJECT_RANGE_END) {
            Subject subject = subjectRepository.findById(id - SUBJECT_RANGE_START).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No such collection"));
            return new CollectionDetailsJson(subject);
        } else {
            Collection collection = collectionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No such collection"));
            if (!collection.isDisplayed()) throw new ResponseStatusException(NOT_FOUND, "Title not deliverable");
            List<Long> agencyIds = agencyRepository.findIdsByCollection(collection);
            List<Agency> agencies = agencyRepository.findAllByIdPreserveOrder(agencyIds);
            TimeFrame timeFrame = collection.getInheritedTimeFrame();
            List<Instance> snapshots;
            if (timeFrame == null || timeFrame.startDate() == null) {
                snapshots = instanceRepository.findByCollection(collection);
            } else {
                snapshots = instanceRepository.findByCollectionAt(collection, timeFrame.startDate());
            }
            
            var titleCountByCollectionId = new HashMap<Long,Long>();
            collectionRepository.countDescendentTitlesOfChildren(id).forEach(c -> titleCountByCollectionId.put(c.getId(), c.getCount()));
            var subcollections = new ArrayList<CollectionJson>();
            for (var child : collection.getChildren()) {
                if (!child.isDisplayed()) continue;
                long titleCount = titleCountByCollectionId.getOrDefault(child.getId(), 0L);
                if (titleCount == 0) continue;
                subcollections.add(new CollectionJson(child, titleCount));
            }
            return new CollectionDetailsJson(collection, subcollections, agencies, snapshots, timeFrame);
        }
    }

    @GetMapping(value = "/api/collection/max-id", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long collectionMaxId() {
        return collectionRepository.maxId();
    }

    @GetMapping("/api/logo/{agencyId}")
    @ResponseBody
    public ResponseEntity<byte[]> agencyLogo(@PathVariable long agencyId) {
        var agency = agencyRepository.findById(agencyId).orElseThrow();
        byte[] logo = agency.getLogo();
        return ResponseEntity.ok()
                .contentType(switch (logo[0]) {
                    case 'G' -> MediaType.IMAGE_GIF;
                    case (byte) 0xff -> MediaType.IMAGE_JPEG;
                    case (byte) 0x89 -> MediaType.IMAGE_PNG;
                    default -> MediaType.APPLICATION_OCTET_STREAM;
                })
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .body(logo);
    }

    private static Pattern urlPattern = Pattern.compile("http://pandora.nla.gov.au/pan/([0-9]+)/([0-9-]+)/(.*)");

    @GetMapping(value = "/api/capture", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CaptureDetailsJson capture(@RequestParam("url") String url, @RequestParam("date") String dateParam) throws IOException {
        Instant date;
        if (dateParam.contains("T")) {
            date = Instant.parse(dateParam);
        } else {
            date = DateFormats.ARC_DATE.parse(dateParam, Instant::from);
        }

        Map<Long, Title> titles = new HashMap<>();
        CrawlJson crawl = null;


        Matcher m = urlPattern.matcher(url);
        if (m.matches()) {
            long pi = Long.parseLong(m.group(1));
            String instanceDate = m.group(2);

            if (instanceDate.length() <= 8) {
                instanceDate += "-0000";
            }
            LocalDateTime localDate = LocalDateTime.parse(instanceDate, DateFormats.PANDAS_DATE);

            crawl = new CrawlJson(pi, Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant()));
            var title = titleRepository.findByPi(pi).orElse(null);
            if (title != null) {
                titles.put(title.getId(), title);
            }
        }

        titleRepository.findByUrl(url).forEach(title -> titles.put(title.getId(), title));

        var collections = titles.values().stream()
                .flatMap(title -> title.getCollections().stream())
                .distinct()
                .sorted(comparing(Collection::getFullName))
                .map(LegacyCollectionJson::new)
                .toList();

        var agencies = titles.values().stream()
                .flatMap(title -> AgencyJson.fromOwnershipHistory(title).stream())
                .collect(Collectors.toList());

        // checkAccessBulk(singletonList(capture));

        var jsonTitles = titles.values().stream().sorted(comparing(Title::getName)).map(TitleJson::new).toList();

        CaptureDetailsJson captureDetailsJson = new CaptureDetailsJson(url, date, crawl, jsonTitles, collections, agencies);
        accessChecker.checkAccessBulk(List.of(captureDetailsJson));

        return captureDetailsJson;
    }

    private CollectionDetailsJson topCollection() {
        return new CollectionDetailsJson(
                TOP_COLLECTION_ID,
                TOP_COLLECTION_NAME,
                "The <strong>Australian Web Archive</strong> captures over twenty years of our cultural and social " +
                "history in the form of billions of webpage snapshots. " +
                "<a href='https://trove.nla.gov.au/help/categories/websites-category'>Learn more</a>.",
                subjectRepository.findByParentIsNullOrderByName());
    }

    public static class CaptureDetailsJson implements AccessChecker.Restrictable {
        public final String url;
        @JsonFormat(pattern = JSON_DATE_FORMAT)
        public final Date date;
        public final CrawlJson crawl;
        public final List<TitleJson> titles;
        public final List<LegacyCollectionJson> collections;
        public final List<AgencyJson> agencies;

        public boolean restricted;
        public String restrictionMessage;

        public CaptureDetailsJson(String url, Instant date, CrawlJson crawl, List<TitleJson> titles,
                                  List<LegacyCollectionJson> collections, List<AgencyJson> agencies) {
            this.url = url;
            this.date = Date.from(date);
            this.crawl = crawl;
            this.titles = titles;
            this.collections = collections;
            this.agencies = agencies;
        }

        @Override
        public AccessChecker.Query toAccessQuery() {
            return new AccessChecker.Query(url, date.toInstant());
        }

        @Override
        public void handleAccessDecision(AccessChecker.Decision decision) {
            restricted = !decision.isAllowed();
            restrictionMessage = decision.getRule().getPublicMessage();
        }
    }

    public static class CrawlJson {
        public final long titleId;
        @JsonFormat(pattern = JSON_DATE_FORMAT)
        public final Date date;

        public CrawlJson(long titleId, Date date) {
            this.titleId = titleId;
            this.date = date;
        }
    }

    public static class TitleJson {
        public final long id;
        public final String name;
        public final String url;
        public final String bibUrl;

        public TitleJson(Title title) {
            id = title.getPi();
            name = title.getDisplayName();
            url = title.getTitleUrl();
            bibUrl = title.getAnbdNumber() == null ? null : "http://nla.gov.au/anbd.bib-an" + title.getAnbdNumber();
        }
    }

    public static class TitleDetailsJson extends TitleJson {
        public final List<AgencyJson> agencies;
        public final List<LegacyCollectionJson> collections;
        public final String contentWarning;
        public final List<TitleHistoryJson> continuedBy;
        public final List<TitleHistoryJson> continues;
        public final String copyrightNote;
        public final List<InstanceJson> instances;
        public final List<IssueGroupJson> issueGroups;
        public final String note;
        public final List<String> previousNames;
        public final boolean disappeared;

        public TitleDetailsJson(Title title) {
            super(title);
            agencies = AgencyJson.fromOwnershipHistory(title);
            collections = title.getCollections().stream().map(LegacyCollectionJson::new).toList();
            contentWarning = title.getContentWarning();
            continuedBy = title.getContinuedBy().stream().map(th -> new TitleHistoryJson(th, title.getName())).toList();
            continues = title.getContinues().stream().map(th -> new TitleHistoryJson(th, title.getName())).toList();
            copyrightNote = title.getTep().getCopyrightNote();
            disappeared = title.isDisappeared();
            instances = title.getInstances().stream()
                    .sorted(Comparator.comparing(Instance::getDate).reversed())
                    .map(i -> new InstanceJson(i, null)).toList();
            issueGroups = title.getTep().getIssueGroups().stream().map(IssueGroupJson::new).toList();
            note = title.getTep().getGeneralNote();
            previousNames = title.getPreviousNames().stream().map(TitlePreviousName::getName).toList();
        }
    }

    public static class AgencyJson {
        public final long id;
        public final String name;
        public final String alias;
        @JsonFormat(pattern = JSON_DATE_FORMAT)
        public final Date ownershipDate;
        public final String url;

        public AgencyJson(Agency agency, Instant ownershipDate) {
            id = agency.getId();
            name = agency.getName();
            alias = agency.getOrganisation().getAlias();
            this.ownershipDate = ownershipDate == null ? null : Date.from(ownershipDate);
            url = agency.getOrganisation().getUrl();
        }

        public static List<AgencyJson> fromOwnershipHistory(Title title) {
            final List<AgencyJson> agencies;
            var distinctAgencyIds = new HashSet<Long>();
            agencies = title.getOwnerHistories().stream()
                    .filter(oh -> oh.getAgency() != null && distinctAgencyIds.add(oh.getAgency().getId()))
                    .map(oh -> new AgencyJson(oh.getAgency(), oh.getDate()))
                    .toList();
            return agencies;
        }
    }

    public static class TitleHistoryJson {
        public final long id;
        public final String name;
        @JsonFormat(pattern = JSON_DATE_FORMAT)
        public final Date dateChanged;

        public TitleHistoryJson(TitleHistory titleHistory, String name) {
            id = titleHistory.getId();
            dateChanged = Date.from(titleHistory.getDate());
            this.name = name;
        }
    }

    public static class CollectionJson {
        public final long id;
        public final String name;
        public final Long numberOfItems;
        public final Long thumbnailCollectionId;
        public final String thumbnailUrl;

        public CollectionJson(long id, String name) {
            this.id = id;
            this.name = name;
            this.numberOfItems = null;
            this.thumbnailCollectionId = null;
            this.thumbnailUrl = null;
        }

        public CollectionJson(Subject subject) {
            id = subject.getId() + SUBJECT_RANGE_START;
            assert id < SUBJECT_RANGE_END;
            name = subject.getName();
            numberOfItems = subject.getChildren().size() + subject.getCollectionCount();
            thumbnailCollectionId = null; // FIXME
            thumbnailUrl = null; // FIXME
        }

        public CollectionJson(Collection collection) {
            this(collection, null);
        }

        public CollectionJson(Collection collection, Long titleCount) {
            id = collection.getId();
            name = collection.getName();
            numberOfItems = titleCount;
            this.thumbnailCollectionId = null; // FIXME
            this.thumbnailUrl = null; // FIXME
        }
    }

    public static class LegacyCollectionJson {
        public final long id;
        public final String name;
        public final Long parentId;
        public final String parentName;
        public final Long numberOfItems;

        public LegacyCollectionJson(Collection collection) {
            id = collection.getId();
            name = collection.getName();
            if (collection.getParent() == null) {
                parentId = null;
                parentName = null;
            } else {
                parentId = collection.getParent().getId();
                parentName = collection.getParent().getName();
            }
            numberOfItems = collection.getTitleCount(); // FIXME: only count deliverable titles
        }

        public LegacyCollectionJson(long id, String name) {
            this.id = id;
            this.name = name;
            parentId = null;
            parentName = null;
            numberOfItems = null;
        }

    }

    public static class CollectionDetailsJson extends CollectionJson {
        @JsonFormat(pattern = JSON_DATE_FORMAT)
        public final Date startDate;
        @JsonFormat(pattern = JSON_DATE_FORMAT)
        public final Date endDate;
        public final String description;
        public final List<CollectionJson> subcollections;
        public final List<LegacyCollectionJson> related;
        public final List<BreadcrumbJson> breadcrumbs;
        public final List<AgencyJson> agencies;
        public final List<InstanceJson> snapshots;

        public CollectionDetailsJson(Collection collection, List<CollectionJson> subcollections, List<Agency> agencies, List<Instance> snapshots, TimeFrame timeFrame) {
            super(collection, null);
            Instant startDate = timeFrame == null ? null : timeFrame.startDate();
            Instant endDate = timeFrame == null ? null : timeFrame.endDate();
            this.startDate = startDate == null ? null : Date.from(startDate);
            this.endDate = endDate == null ? null : Date.from(endDate);
            description = collection.getDescription();
            this.subcollections = subcollections;
            related = List.of();
            breadcrumbs = buildBreadcrumbList(collection);
            this.agencies = agencies.stream().map(a -> new AgencyJson(a, null)).toList();
            this.snapshots = snapshots.stream().map(i -> new InstanceJson(i, i.getTitle().getName())).toList();
        }

        public CollectionDetailsJson(long id, String name, String description, List<Subject> topLevelSubjects) {
            super(id, name);
            this.description = description;
            breadcrumbs = List.of(new BreadcrumbJson(id, name));

            startDate = null;
            endDate = null;
            related = Collections.emptyList();
            agencies = Collections.emptyList();
            snapshots = Collections.emptyList();
            subcollections = topLevelSubjects.stream().map(CollectionJson::new).toList();
        }

        public CollectionDetailsJson(Subject subject) {
            super(subject);
            description = subject.getDescription();
            breadcrumbs = buildBreadcrumbList(subject);

            subcollections = Stream.concat(
                            subject.getChildren().stream()
                                    .map(CollectionJson::new)
                                    .filter(json -> json.numberOfItems == null || json.numberOfItems != 0),
                            subject.getCollections().stream()
                                    .filter(collection -> collection.isDisplayed() && collection.getParent() == null)
                                    .map(CollectionJson::new))
                    .sorted(Comparator.comparing(c -> c.name))
                    .toList();

            startDate = null;
            endDate = null;
            related = Collections.emptyList();
            agencies = Collections.emptyList();
            snapshots = Collections.emptyList();
        }
    }

    public static List<BreadcrumbJson> buildBreadcrumbList(Collection collection) {
        var breadcrumbs = new ArrayList<BreadcrumbJson>();

        while (collection != null) {
            breadcrumbs.add(new BreadcrumbJson(collection));

            if (collection.getParent() == null && !collection.getSubjects().isEmpty()) {
                Subject subject = collection.getSubjects().get(0);
                while (subject != null) {
                    breadcrumbs.add(new BreadcrumbJson(subject));
                    subject = subject.getParent();
                }
            }

            collection = collection.getParent();
        }

        breadcrumbs.add(new BreadcrumbJson(TOP_COLLECTION_ID, TOP_COLLECTION_NAME));

        Collections.reverse(breadcrumbs);
        return Collections.unmodifiableList(breadcrumbs);
    }

    public static List<BreadcrumbJson> buildBreadcrumbList(Subject subject) {
        var breadcrumbs = new ArrayList<BreadcrumbJson>();

        while (subject != null) {
            breadcrumbs.add(new BreadcrumbJson(subject));
            subject = subject.getParent();
        }

        breadcrumbs.add(new BreadcrumbJson(TOP_COLLECTION_ID, TOP_COLLECTION_NAME));

        Collections.reverse(breadcrumbs);
        return Collections.unmodifiableList(breadcrumbs);
    }

    public static class BreadcrumbJson {
        public final long id;
        public final String name;

        public BreadcrumbJson(Collection collection) {
            id = collection.getId();
            name = collection.getName();
        }

        public BreadcrumbJson(Subject subject) {
            id = subject.getId() + SUBJECT_RANGE_START;
            name = subject.getName();
            assert id < SUBJECT_RANGE_END;
        }

        public BreadcrumbJson(long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class InstanceJson implements AccessChecker.Restrictable {
        public final String gatheredUrl;
        public final String gatherMethod;
        public final String tepUrl;
        @JsonFormat(pattern = JSON_DATE_FORMAT)
        public final Date date;
        public final String link;
        public final String url;
        public final String name;

        public boolean restricted;
        public String restrictionMessage;

        public InstanceJson(Instance instance, String name) {
            gatheredUrl = instance.getGatheredUrl();
            gatherMethod = instance.getGatherMethodName();
            tepUrl = instance.getTepUrl();
            date = Date.from(instance.getDate());
            link = Util.buildLink(gatherMethod, tepUrl, gatheredUrl);
            url = link;
            this.name = name;
        }

        @Override
        public AccessChecker.Query toAccessQuery() {
            return new AccessChecker.Query(url, date.toInstant());
        }

        @Override
        public void handleAccessDecision(AccessChecker.Decision decision) {
            restricted = !decision.isAllowed();
            restrictionMessage = decision.getRule().getPublicMessage();
        }
    }

    public static class IssueGroupJson {
        public final long id;
        public final String name;
        public final String note;
        public final List<IssueJson> issues;

        public IssueGroupJson(IssueGroup issueGroup) {
            id = issueGroup.getId();
            name = issueGroup.getName();
            note = issueGroup.getNotes();
            issues = issueGroup.getIssues().stream().map(IssueJson::new).toList();
        }
    }

    private static final Pattern REPLAY_URL_PATTERN = Pattern.compile("https://[^/]+/awa/([0-9]{14})/.*");
    private static final Pattern PANDORA_URL_PATTERN = Pattern.compile(".*/pandora\\.nla\\.gov\\.au/pan/[0-9]+/([0-9]{8}(?:-[0-9]{4})?)/.*");
    private static final DateTimeFormatter PAN_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm", Locale.US).withZone(ZoneId.of("Australia/Sydney"));

    public static class IssueJson implements AccessChecker.Restrictable {
        public final long id;
        public final String name;
        public final String url;
        @JsonFormat(pattern = JSON_DATE_FORMAT)
        public final Date date;

        public boolean restricted;
        public String restrictionMessage;

        public IssueJson(Issue issue) {
            id = issue.getId();
            name = issue.getName();
            url = Util.applyRewriteRules(issue.getUrl(), false);

            // try to parse date from url
            Date date = null;
            if (url != null) {
                try {
                    Matcher m;
                    if ((m = REPLAY_URL_PATTERN.matcher(url)).matches()) {
                        date = Date.from(DateFormats.ARC_DATE.parse(m.group(1), Instant::from));
                    } else if ((m = PANDORA_URL_PATTERN.matcher(url)).matches()) {
                        String string = m.group(1);
                        if (!string.contains("-")) {
                            string += "-0000";
                        }
                        date = Date.from(PAN_DATE.parse(string, Instant::from));
                    }
                } catch (DateTimeParseException e) {
                    // aww
                }
            }
            if (date == null) {
                // this probably means the issue has a bogus url so its probably broken anyway
                // but trove will probably NullPointException if we return null so just
                // return a placeholder epoch date.
                date = new Date(0);
            }
            this.date = date;
        }

        @Override
        public AccessChecker.Query toAccessQuery() {
            return new AccessChecker.Query(url, date.toInstant());
        }

        @Override
        public void handleAccessDecision(AccessChecker.Decision decision) {
            restricted = !decision.isAllowed();
            restrictionMessage = decision.getRule().getPublicMessage();
        }
    }
}
