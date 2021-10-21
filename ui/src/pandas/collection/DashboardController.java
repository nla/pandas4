package pandas.collection;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pandas.core.Individual;
import pandas.core.UserService;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class DashboardController {
    private final CollectionRepository collectionRepository;
    private final TitleRepository titleRepository;
    private final UserService userService;

    public DashboardController(CollectionRepository collectionRepository, TitleRepository titleRepository, UserService userService) {
        this.collectionRepository = collectionRepository;
        this.titleRepository = titleRepository;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Individual currentUser = userService.getCurrentUser();

        var activityPeriods = createTimePeriods();
        Instant dateLimit = activityPeriods.get(activityPeriods.size() - 1).instant;

        { // partition the nominations
            var periodIterator = activityPeriods.iterator();
            ActivityPeriod period = periodIterator.next();
            outer: for (Title title : titleRepository.findByNominator(currentUser, dateLimit)) {
                while (title.getRegDate().isBefore(period.instant)) {
                    if (!periodIterator.hasNext()) break outer;
                    period = periodIterator.next();
                }
                period.nominations.add(title);
            }
        }

        { // partition the selections
            var periodIterator = activityPeriods.iterator();
            ActivityPeriod period = periodIterator.next();
            outer: for (Title title : titleRepository.findBySelector(currentUser, dateLimit)) {
                while (title.getRegDate().isBefore(period.instant)) {
                    if (!periodIterator.hasNext()) break outer;
                    period = periodIterator.next();
                }
                period.selections.add(title);
            }
        }

        { // partition the collections
            var periodIterator = activityPeriods.iterator();
            ActivityPeriod period = periodIterator.next();
            outer: for (var collection : collectionRepository.findByCreatedByAndCreatedDateIsAfterOrderByCreatedDateDesc(currentUser, dateLimit)) {
                while (collection.getCreatedDate().isBefore(period.instant)) {
                    if (!periodIterator.hasNext()) break outer;
                    period = periodIterator.next();
                }
                period.collections.add(collection);
            }
        }

        activityPeriods.removeIf(ActivityPeriod::isEmpty);

        model.addAttribute("activityPeriods", activityPeriods);
        return "Dashboard";
    }


    private ArrayList<ActivityPeriod> createTimePeriods() {
        var periods = new ArrayList<ActivityPeriod>();
        var today = LocalDate.now();
        periods.add(new ActivityPeriod("Today", today));
        var startOfWeek = today.with(DayOfWeek.MONDAY);

        var yesterday = today.minusDays(1);
        if (!yesterday.isBefore(startOfWeek)) {
            periods.add(new ActivityPeriod("Yesterday", today.minusDays(1)));
            for (var day = today.minusDays(2); !day.isBefore(startOfWeek); day = day.minusDays(1)) {
                periods.add(new ActivityPeriod(day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()), day));
            }
        }

        var startOfLastWeek = startOfWeek.minusDays(7);
        periods.add(new ActivityPeriod("Last Week", startOfLastWeek));

        var startOfThisMonth = today.withDayOfMonth(1);
        if (startOfThisMonth.isBefore(startOfLastWeek)) {
            periods.add(new ActivityPeriod("This Month", startOfThisMonth));
        }
        periods.add(new ActivityPeriod("Last Month", startOfThisMonth.minusMonths(1)));
        return periods;
    }

    public static class ActivityPeriod {
        public final String name;
        public final Instant instant;
        public final List<Title> nominations = new ArrayList<>();
        public final List<Title> selections = new ArrayList<>();
        public final List<Collection> collections = new ArrayList<>();

        public ActivityPeriod(String name, LocalDate startDate) {
            this.name = name;
            this.instant = startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        }

        public boolean isEmpty() {
            return nominations.isEmpty() && selections.isEmpty() && collections.isEmpty();
        }
    }

}
