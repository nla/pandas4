package pandas.cli;

import com.google.common.collect.Iterables;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.transaction.annotation.Transactional;
import pandas.collection.UrlStats;
import pandas.collection.UrlStatsRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@ShellComponent
public class UrlStatsCommands {
    private final UrlStatsRepository urlStatsRepository;

    public UrlStatsCommands(UrlStatsRepository urlStatsRepository) {
        this.urlStatsRepository = urlStatsRepository;
    }

    public static UrlStats parseLine(String line) {
        String[] fields = line.split(" ");
        if (fields.length < 5) return null;

        UrlStats record = new UrlStats();
        record.setYear(Integer.parseInt(fields[0]));
        record.setSite(fields[1]);
        record.setContentType(fields[2]);
        record.setSnapshots(Long.parseLong(fields[3]));
        record.setTotalContentLength(Long.parseLong(fields[4]));
        return record;
    }

    @ShellMethod(value = "Load urlstats data from a space-separated text file (year, site, content-type, snapshots, sum-of-content-lengths)")
    @Transactional
    public void loadUrlstats(String file) throws IOException {
        System.out.println("hello");

        long rows = 0;
        try (var reader = Files.newBufferedReader(Paths.get(file))) {
            var urlStatsStream = reader.lines()
                    .map(UrlStatsCommands::parseLine)
                    .filter(Objects::nonNull);
            for (List<UrlStats> batch : Iterables.partition(urlStatsStream::iterator, 1000)) {
                urlStatsRepository.saveAll(batch);
                rows += batch.size();
                System.out.println(rows);
            }
        }

    }
}
