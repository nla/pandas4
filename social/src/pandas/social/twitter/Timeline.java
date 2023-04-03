package pandas.social.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Timeline(List<Instruction> instructions) {
    List<Entry> allEntries() {
        List<Entry> entries = new ArrayList<>();
        for (var instruction : instructions) {
            if (instruction instanceof AddEntries addEntries) {
                entries.addAll(addEntries.entries());
            } else if (instruction instanceof ReplaceEntry replaceEntry) {
                entries.add(replaceEntry.entry());
            }
        }
        return entries;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = AddEntries.class, name = "addEntries"),
            @JsonSubTypes.Type(value = ReplaceEntry.class, name = "replaceEntry")})
    public interface Instruction {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddEntries(List<Entry> entries) implements Instruction {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReplaceEntry(Entry entry) implements Instruction {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(String entryId, TimelineContent content) {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Operation.class, name = "operation"),
            @JsonSubTypes.Type(value = Item.class, name = "item")})
    public interface TimelineContent {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Operation(Cursor cursor) implements TimelineContent {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(ItemContent content) implements TimelineContent {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TweetRef.class, name = "tweet")})
    public interface ItemContent {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TweetRef(String id) implements ItemContent {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cursor(String value, String cursorType) {
    }
}
