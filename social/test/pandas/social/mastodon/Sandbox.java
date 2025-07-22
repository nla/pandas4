package pandas.social.mastodon;

import com.fasterxml.jackson.databind.ObjectMapper;
import pandas.social.SocialJson;

import java.nio.file.Path;

public class Sandbox {
    public static void main(String[] args) throws Exception {
        var statuses = SocialJson.mapper.readValue(Path.of("data/social.json").toFile(), Status[].class);
    }
}
