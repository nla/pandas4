package pandas.cli;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import pandas.agency.UserRepository;
import pandas.collection.OwnerHistory;
import pandas.collection.OwnerHistoryRepository;
import pandas.collection.TitleRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

@ShellComponent
public class TitleCommands {
    private final UserRepository userRepository;
    private final TitleRepository titleRepository;
    private final OwnerHistoryRepository ownerHistoryRepository;

    public TitleCommands(UserRepository userRepository, TitleRepository titleRepository, OwnerHistoryRepository ownerHistoryRepository) {
        this.userRepository = userRepository;
        this.titleRepository = titleRepository;
        this.ownerHistoryRepository = ownerHistoryRepository;
    }

    @ShellMethod(value = "Create owner history records")
    public String createOwnerHistory(String date, long ownerId, Long[] titleIds) {
        var instant = Instant.parse(date);
        var owner = userRepository.findById(ownerId).orElseThrow();
        var titles = titleRepository.findAllById(Arrays.asList(titleIds));
        var histories = new ArrayList<OwnerHistory>();
        for (var title: titles) {
            var oh = new OwnerHistory(title, title.getAgency(), owner, "Bulk change", owner, instant);
            histories.add(oh);
            System.out.println(oh);
        }
        ownerHistoryRepository.saveAll(histories);
        return "Saved " + ownerId + " " + Arrays.toString(titleIds);
    }
}
