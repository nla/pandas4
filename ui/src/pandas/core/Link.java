package pandas.core;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import pandas.agency.Agency;
import pandas.agency.AgencySummary;
import pandas.agency.User;
import pandas.collection.*;
import pandas.gather.Instance;
import pandas.gather.InstanceSeed;
import pandas.gather.PreviousGather;
import pandas.util.DateFormats;

import java.net.URLEncoder;
import java.time.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encodePathSegment;

@Service
public class Link {

    @Value("${pandas.deliveryBaseUrl:https://webarchive.nla.gov.au/awa/}")
    private String deliveryBaseUrl = "https://webarchive.nla.gov.au/awa/";

    private final String bambooBaseUrl;

    public Link(Config config) {
        this.bambooBaseUrl = config.getBambooUrl().replaceFirst("/+$", "");
    }

    private String link(String path) {
        return servletRequest().getContextPath().replaceFirst("/+$", "") + path;
    }

    private HttpServletRequest servletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    public String checkSessionReply() {
        return ServletUriComponentsBuilder.fromContextPath(servletRequest()).path("/login/check-session-reply").toUriString();
    }

    public String delivery(Instant date, String url) {
        return deliveryBaseUrl + (date == null ? "*" : DateFormats.ARC_DATE.format(date)) + "/" + url;
    }

    public String delivery(Instance instance) {
        if (instance.getState().isArchived()) {
            return delivery(instance.getDate(), instance.getTepUrlAbsolute());
        } else {
            return to(instance) + "/process";
        }
    }

    public String delivery(InstanceSeed seed) {
        if (seed.getInstance().getState().isArchived()) {
            return delivery(seed.getInstance().getDate(), seed.getUrl());
        } else {
            return to(seed.getInstance()) + "/process?url=" + URLEncoder.encode(seed.getUrl(), UTF_8);
        }
    }

    public String delivery(Issue issue) {
        return delivery(issue.getDate(), issue.getDeliveryUrl());
    }

    public String edit(Agency agency) {
        return to(agency) + "/edit";
    }

    public String edit(ContactPerson contactPerson) {
        return link("/contact-people/" + contactPerson.getId() + "/edit");
    }

    public String edit(Instance instance) {
        return to(instance) + "/edit";
    }

    public String edit(User user) {
        return to(user) + "/edit";
    }

    public String edit(Title title) {
        return to(title) + "/edit";
    }

    public String edit(Title title, ContactPerson contactPerson) {
        return to(title) + "/contact-people/" + contactPerson.getId() + "/edit";
    }


    public String flag(Title title) {
        return to(title) + "/flag";
    }

    public String files(Instance instance) {
        return to(instance) + "/files";
    }

    public String icon(Subject subject) {
        return to(subject) + "/icon";
    }

    public String subjectIcon(long subjectId) {
        return link("/subjects/" + subjectId + "/icon");
    }

    public String logo(Agency agency) {
        return to(agency) + "/logo";
    }

    public String publishers(Collection collection) {
        return to(collection) + "/publishers";
    }

    public String thumbnail(Instance instance) {
        return to(instance) + "/thumbnail";
    }

    public String thumbnail(Title title) {
        return to(title) + "/thumbnail/image";
    }

    public String thumbnail(Instance instance, String type) {
        return to(instance) + "/thumbnail?type=" + type;
    }

    public String thumbnail(PreviousGather previousGather, String type) {
        return to(previousGather) + "/thumbnail?type=" + type;
    }

    public String to(AgencySummary agencySummary) {
        return link("/agencies/" + agencySummary.getId());
    }

    public String to(Agency agency) {
        return link("/agencies/" + agency.getId());
    }

    public String to(Collection collection) {
        return link("/collections/" + collection.getId());
    }

    public String to(User user) {
        return link("/users/" + encodePathSegment(user.getUserid(), UTF_8));
    }

    public String to(Instance instance) {
        return toInstance(instance.getId());
    }

    public String toInstance(long instanceId) {
        return link("/instances/" + instanceId);
    }

    public String to(Permission permission) {
        return link("/permissions/" + permission.getId());
    }

    public String to(PreviousGather previousGather) {
        return link("/instances/" + previousGather.getId());
    }

    public String to(Publisher publisher) {
        return link("/publishers/" + publisher.getId());
    }

    public String toPublisher(long publisherId) {
        return link("/publishers/" + publisherId);
    }

    public String to(Subject subject) {
        return link("/subjects/" + subject.getId());
    }

    public String to(Title title) {
        return link("/titles/" + title.getId());
    }

    public String toBambooCrawl(long crawlId) {
        return bambooBaseUrl + "/crawls/" + crawlId;
    }

    public String toBambooWarc(String filename) {
        return bambooBaseUrl + "/warcs/" + UriUtils.encodePathSegment(filename, UTF_8) + "/details";
    }

    public String unflag(Title title) {
        return to(title) + "/unflag";
    }
}
