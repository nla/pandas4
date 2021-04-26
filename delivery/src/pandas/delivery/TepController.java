package pandas.delivery;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pandas.collection.Issue;
import pandas.collection.IssueGroup;
import pandas.collection.Title;
import pandas.collection.TitleRepository;
import pandas.gather.InstanceRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

@Controller
public class TepController {
    private final TitleRepository titleRepository;
    private final InstanceRepository instanceRepository;
    private final CdxClient cdxClient;

    public TepController(TitleRepository titleRepository, InstanceRepository instanceRepository, CdxClient cdxClient) {
        this.titleRepository = titleRepository;
        this.instanceRepository = instanceRepository;
        this.cdxClient = cdxClient;
    }

    @GetMapping("/tep/{pi}")
    public String tep(@PathVariable("pi") long pi, Model model) {
        Title title = titleRepository.findByPi(pi).orElseThrow();
        var instances = instanceRepository.findDisplayedByTitle(title);

        // fetch restrictions of displayed instances and instances reference by issues
        var instanceSet = new HashSet<>(instances);
        for (IssueGroup group : title.getTep().getIssueGroups()) {
            for (Issue issue : group.getIssues()) {
                instanceSet.add(issue.getInstance());
            }
        }
        var restrictions = cdxClient.checkInstanceRestrictions(new ArrayList<>(instanceSet));

        model.addAttribute("title", title);
        model.addAttribute("instances", instances);
        model.addAttribute("restrictions", restrictions);
        model.addAttribute("dateTimeFormat", DateTimeFormatter.ofPattern("dd MMM uuuu hh:mm a").withZone(ZoneId.systemDefault()));
        return "Tep";
    }
}
