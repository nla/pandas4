package pandas.cli;

import com.google.common.collect.Iterables;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
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

    private JdbcTemplate jdbcTemplate;

    public UrlStatsCommands(JdbcTemplate jdbcTemplate, UrlStatsRepository urlStatsRepository) {
        this.jdbcTemplate = jdbcTemplate;
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

    public static Object[] parseLine2(String line) {
        String[] fields = line.split(" ");
        if (fields.length < 5) return null;
        int year = Integer.parseInt(fields[0]);
        String site = fields[1];
        String contentType = fields[2];
        long snapshots = Long.parseLong(fields[3]);
        long totalContentLength = Long.parseLong(fields[4]);
        return new Object[]{contentType, site, year, snapshots, totalContentLength};
    }

    @ShellMethod(value = "Load urlstats data from a space-separated text file (year, site, content-type, snapshots, sum-of-content-lengths)")
    @Transactional
    public void loadUrlstats(String file, @ShellOption(defaultValue = "1000") int batchSize) throws IOException {
        System.out.println("Loading url_stats from " + file + " in batches of " + batchSize);

        long rows = 0;
        try (var reader = Files.newBufferedReader(Paths.get(file))) {
            var urlStatsStream = reader.lines()
                    .map(UrlStatsCommands::parseLine2)
                    .filter(Objects::nonNull);
            jdbcTemplate.execute("delete from url_stats");
            for (List<Object[]> batch : Iterables.partition(urlStatsStream::iterator, batchSize)) {
                jdbcTemplate.batchUpdate("insert into url_stats (content_type, site, year, snapshots, total_content_length) values (?, ?, ?, ?, ?)", batch);
                rows += batch.size();
                System.out.println(rows);
            }
        }

    }
}
