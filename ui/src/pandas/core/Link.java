package pandas.core;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pandas.agency.Agency;
import pandas.collection.Publisher;
import pandas.collection.Subject;
import pandas.collection.Title;
import pandas.gather.Instance;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encodePathSegment;

@Service
public class Link {
    private String link(String path) {
        String contextPath = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getContextPath();
        return contextPath.replaceFirst("/+$", "") + path;
    }

    public String edit(Agency agency) {
        return to(agency) + "/edit";
    }

    public String edit(Individual individual) {
        return to(individual) + "/edit";
    }

    public String logo(Agency agency) {
        return to(agency) + "/logo";
    }

    public String thumbnail(Instance instance) {
        return to(instance) + "/thumbnail";
    }

    public String to(Agency agency) {
        return link("/agencies/" + agency.getOrganisation().getAlias());
    }

    public String to(Individual individual) {
        return link("/users/" + encodePathSegment(individual.getUserid(), UTF_8));
    }

    public String to(Instance instance) {
        return link("/instances/" + instance.getId());
    }

    public String to(Publisher publisher) {
        return link("/publishers/" + publisher.getId());
    }

    public String to(Subject subject) {
        return link("/subjects/" + subject.getId());
    }

    public String to(Title title) {
        return link("/titles/" + title.getId());
    }
}
