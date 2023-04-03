package pandas.social.generic;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public record Post(
        URI url,
        Instant createdAt,
        Account author,
        Site site,
        String to,
        List<String> mentions,
        String content,
        @NotNull List<Attachment> attachments,
        Long replyCount,
        Long reblogCount,
        Long quoteCount,
        Long favouriteCount) {
    public String contentSafe() {
        return Jsoup.clean(content, "", Safelist.basic(), new Document.OutputSettings().prettyPrint(false));
    }

    public String createdDate() {
        return createdAt.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public String createdDateTime() {
        return createdAt.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG));
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    public List<Statistic> statistics() {
        return List.of(
                new Statistic("replies", "🗨", replyCount),
                new Statistic("reblogs", "↻", reblogCount),
                new Statistic("quotes", "❞", quoteCount),
                new Statistic("likes", "❤", favouriteCount));

    }

    public record Statistic(String name, String emoji, Long count) {
        public String shortText() {
            return String.format("%s %,d", emoji, count);
        }

        public String longText() {
            return String.format("%,d %s", count, name);
        }
    }
}
