package pandas.cli;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import pandas.collection.OwnerHistory;
import pandas.collection.OwnerHistoryRepository;
import pandas.collection.TitleRepository;
import pandas.core.IndividualRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

@ShellComponent
public class TitleCommands {
    private final IndividualRepository individualRepository;
    private final TitleRepository titleRepository;
    private final OwnerHistoryRepository ownerHistoryRepository;

    public TitleCommands(IndividualRepository individualRepository, TitleRepository titleRepository, OwnerHistoryRepository ownerHistoryRepository) {
        this.individualRepository = individualRepository;
        this.titleRepository = titleRepository;
        this.ownerHistoryRepository = ownerHistoryRepository;
    }

    @ShellMethod(value = "Create owner history records")
    public String createOwnerHistory(String date, long ownerId, Long[] titleIds) {
        var instant = Instant.parse(date);
        var owner = individualRepository.findById(ownerId).orElseThrow();
        var titles = titleRepository.findAllById(Arrays.asList(titleIds));
        var histories = new ArrayList<OwnerHistory>();
        for (var title: titles) {
            var oh = new OwnerHistory();
            oh.setTitle(title);
            oh.setNote("Bulk change");
            oh.setTransferrer(owner);
            oh.setIndividual(owner);
            oh.setAgency(title.getAgency());
            oh.setDate(instant);
            histories.add(oh);
            System.out.println(oh);
        }
        ownerHistoryRepository.saveAll(histories);
        return "Saved " + ownerId + " " + Arrays.toString(titleIds);
    }
}
