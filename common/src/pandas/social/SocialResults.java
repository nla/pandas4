package pandas.social;

import java.util.List;

public record SocialResults(long totalHits, List<Post> posts) {
}
