package pandas.gather;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GatherService {
    private final GatherMethodRepository gatherMethodRepository;
    private final GatherScheduleRepository gatherScheduleRepository;

    public GatherService(GatherMethodRepository gatherMethodRepository, GatherMethodRepository gatherMethodRepository1, GatherScheduleRepository gatherScheduleRepository) {
        this.gatherMethodRepository = gatherMethodRepository1;
        this.gatherScheduleRepository = gatherScheduleRepository;
    }

    public List<GatherSchedule> allGatherSchedules() {
        ArrayList<GatherSchedule> schedules = new ArrayList<>();
        gatherScheduleRepository.findAll().forEach(schedules::add);
        schedules.sort(Comparator.naturalOrder());
        return schedules;
    }

    public GatherMethod defaultMethod() {
        return gatherMethodRepository.findByName("Heritrix");
    }

    public GatherSchedule defaultSchedule() {
        return gatherScheduleRepository.findByName("Quarterly");
    }
}
