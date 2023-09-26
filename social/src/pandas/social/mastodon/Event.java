package pandas.social.mastodon;

import com.fasterxml.jackson.core.JsonProcessingException;
import pandas.social.SocialJson;

import java.io.BufferedReader;
import java.io.IOException;

// https://docs.joinmastodon.org/methods/streaming/#events
public sealed interface Event {
    static Event read(BufferedReader reader) throws IOException {
        String event = null;
        StringBuilder data = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                if (event != null) break;
                return null;
            }
            if (line.isEmpty() && event != null) break;
            if (line.startsWith(":")) continue;
            if (line.startsWith("event: ")) event = line.substring("event: ".length());
            if (line.startsWith("data: ")) {
                if (!data.isEmpty()) data.append('\n');
                data.append(line, "data: ".length(), line.length());
            }
        }
        return Event.parse(event, data.toString());
    }

    static Event parse(String event, String data) throws JsonProcessingException {
        return switch (event) {
            case "delete" -> new Delete(Long.parseLong(data));
            case "status.update" -> new StatusUpdate(SocialJson.mapper.readValue(data, Status.class));
            case "update" -> new Update(SocialJson.mapper.readValue(data, Status.class));
            default -> throw new IllegalArgumentException("Unknown Mastodon event: " + event);
        };
    }

    record Delete(long id) implements Event {
    }

    record StatusUpdate(Status status) implements Event {
    }

    record Update(Status status) implements Event {
    }
}
