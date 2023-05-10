package pandas.social;

import java.io.IOException;
import java.nio.file.Path;

public class AttachmentArchiver {
    public static void main(String[] args) throws IOException {
        for (String arg: args) {
            try (SocialReader socialReader = new SocialReader(Path.of(arg))) {
                for (var batch = socialReader.nextBatch(); batch != null; batch = socialReader.nextBatch()) {
                    for (var post : batch) {
                        visit(post, "");
                    }
                }
            }
        }
    }

    private static void visit(Post post, String prefix) {
        if (post == null) return;

        System.out.println(prefix + post.url() + " " + (post.repost() != null));
        System.out.println(prefix + "  avatar " + post.author().username() + " " + post.author().avatarUrl());
        System.out.println(prefix + "  banner " + post.author().username() + " " + post.author().bannerUrl());

        for (var attachment : post.attachments()) {
            System.out.println(prefix + "  " + attachment.type() + " " + attachment.url());
            for (var source : attachment.sources()) {
                System.out.println(prefix + "    " + source.contentType() + " " + source.width() + "x" +
                        source.height() + " " + source.url() + " " + source.bitrate());
            }
        }

        visit(post.quotedPost(), prefix + "  QP ");
        visit(post.repost(), prefix + "  RP ");
    }
}
