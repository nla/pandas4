package pandas.api;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.collection.Collection;
import pandas.collection.*;
import pandas.gather.Instance;
import pandas.gather.InstanceRepository;
import pandas.util.DateFormats;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final TitleRepository titleRepository;
    private final CollectionRepository collectionRepository;
    private final AgencyRepository agencyRepository;
    private final InstanceRepository instanceRepository;
    private final AccessChecker accessChecker;

    public ApiController(TitleRepository titleRepository, CollectionRepository collectionRepository, AgencyRepository agencyRepository, InstanceRepository instanceRepository, AccessChecker accessChecker) {
        this.titleRepository = titleRepository;
        this.collectionRepository = collectionRepository;
        this.agencyRepository = agencyRepository;
        this.instanceRepository = instanceRepository;
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
        types.add(CollectionJson.class);
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

    @GetMapping(value = "/api/collection/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CollectionDetailsJson collection(@PathVariable("id") long id) {
        Collection collection = collectionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No such collection"));
        if (!collection.isDisplayed()) throw new ResponseStatusException(NOT_FOUND, "Title not deliverable");
        List<Long> agencyIds = agencyRepository.findIdsByCollection(collection);
        List<Agency> agencies = agencyRepository.findAllByIdPreserveOrder(agencyIds);
        List<Instance> snapshots = instanceRepository.findByCollection(collection);
        return new CollectionDetailsJson(collection, agencies, snapshots);
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
        public final List<CollectionJson> collections;
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
            var distinctAgencyIds = new HashSet<Long>();
            agencies = title.getOwnerHistories().stream()
                    .filter(oh -> oh.getAgency() != null && distinctAgencyIds.add(oh.getAgency().getId()))
                    .map(oh -> new AgencyJson(oh.getAgency(), oh.getDate()))
                    .toList();
            collections = title.getCollections().stream().map(CollectionJson::new).toList();
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
        public final Date ownershipDate;
        public final String url;

        public AgencyJson(Agency agency, Instant ownershipDate) {
            id = agency.getId();
            name = agency.getName();
            alias = agency.getOrganisation().getAlias();
            this.ownershipDate = ownershipDate == null ? null : Date.from(ownershipDate);
            url = agency.getOrganisation().getUrl();
        }
    }

    public static class TitleHistoryJson {
        public final long id;
        public final String name;
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
        public final Long parentId;
        public final String parentName;
        public final long numberOfItems;

        public CollectionJson(Collection collection) {
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
    }

    public static class CollectionDetailsJson extends CollectionJson {
        public final Date startDate;
        public final Date endDate;
        public final String description;
        public final List<CollectionJson> subcollections;
        public final List<CollectionJson> related;
        public final List<BreadcrumbJson> breadcrumbs;
        public final List<AgencyJson> agencies;
        public final List<InstanceJson> snapshots;

        public CollectionDetailsJson(Collection collection, List<Agency> agencies, List<Instance> snapshots) {
            super(collection);
            startDate = collection.getStartDate() == null ? null : Date.from(collection.getStartDate());
            endDate = collection.getEndDate() == null ? null : Date.from(collection.getEndDate());
            description = collection.getDescription();
            subcollections = collection.getChildren().stream().map(CollectionJson::new).toList();
            related = List.of();
            breadcrumbs = buildBreadcrumbList(collection);
            this.agencies = agencies.stream().map(a -> new AgencyJson(a, null)).toList();
            this.snapshots = snapshots.stream().map(i -> new InstanceJson(i, i.getTitle().getName())).toList();
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
