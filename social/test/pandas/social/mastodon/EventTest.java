package pandas.social.mastodon;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    @Test
    public void test() throws IOException {
        String stream = """
            :)
            :thump
            :thump
            event: update
            data: {"id":"108914354907984653","created_at":"2022-08-30T23:12:47.000Z","in_reply_to_id":null,"in_reply_to_account_id":null,"sensitive":false,"spoiler_text":"","visibility":"public","language":"en","uri":"https://mstdn.jp/users/aiueohisama/statuses/108914354891945610","url":"https://mstdn.jp/@aiueohisama/108914354891945610","replies_count":0,"reblogs_count":0,"favourites_count":0,"edited_at":null,"content":"<p>処女の子が「処女だからキモい絡み方しちゃうかもだけど許して🥺」なんて言ってるのわたしは見たことない、童貞の甘えだよそれは 嫌われたくないなら最低限のマナーくらい身につけた方がいい</p>","reblog":null,"account":{"id":"272619","username":"aiueohisama","acct":"aiueohisama@mstdn.jp","display_name":"💎🌻陽菜💙💛","locked":false,"bot":false,"discoverable":false,"group":false,"created_at":"2017-04-15T00:00:00.000Z","note":"<p>とっても素直で真面目なOLでし！</p>","url":"https://mstdn.jp/@aiueohisama","avatar":"https://files.mastodon.social/cache/accounts/avatars/000/272/619/original/573669a325c87b8b.jpeg","avatar_static":"https://files.mastodon.social/cache/accounts/avatars/000/272/619/original/573669a325c87b8b.jpeg","header":"https://files.mastodon.social/cache/accounts/headers/000/272/619/original/5d5dad59a9fd1531.jpeg","header_static":"https://files.mastodon.social/cache/accounts/headers/000/272/619/original/5d5dad59a9fd1531.jpeg","followers_count":182,"following_count":20,"statuses_count":1128,"last_status_at":"2022-08-30","emojis":[],"fields":[]},"media_attachments":[],"mentions":[],"tags":[],"emojis":[],"card":null,"poll":null}

            :thump
            event: delete
            data: 107214471804101576
                        
            event: status.update
            data: {"id":"109348684737626801","created_at":"2022-11-15T16:08:30.000Z","in_reply_to_id":null,"in_reply_to_account_id":null,"sensitive":false,"spoiler_text":"","visibility":"public","language":"en","uri":"https://ruby.social/users/chrismo/statuses/109348684454557541","url":"https://ruby.social/@chrismo/109348684454557541","replies_count":0,"reblogs_count":0,"favourites_count":0,"edited_at":"2022-11-15T16:10:43.000Z","content":"<p><a href=\\"https://ruby.social/tags/musicTuesday\\" class=\\"mention hashtag\\" rel=\\"nofollow noopener noreferrer\\" target=\\"_blank\\">#<span>musicTuesday</span></a> </p><p>Here's a solo <a href=\\"https://ruby.social/tags/piano\\" class=\\"mention hashtag\\" rel=\\"nofollow noopener noreferrer\\" target=\\"_blank\\">#<span>piano</span></a> track of mine called Gravity Assist</p><p><a href=\\"https://ruby.social/tags/neoclassical\\" class=\\"mention hashtag\\" rel=\\"nofollow noopener noreferrer\\" target=\\"_blank\\">#<span>neoclassical</span></a> (<a href=\\"https://ruby.social/tags/jazz\\" class=\\"mention hashtag\\" rel=\\"nofollow noopener noreferrer\\" target=\\"_blank\\">#<span>jazz</span></a> ish)</p><p><a href=\\"https://cstudios.bandcamp.com/track/celestia-gravity-assist-no-19-var-2\\" rel=\\"nofollow noopener noreferrer\\" target=\\"_blank\\"><span class=\\"invisible\\">https://</span><span class=\\"ellipsis\\">cstudios.bandcamp.com/track/ce</span><span class=\\"invisible\\">lestia-gravity-assist-no-19-var-2</span></a></p>","reblog":null,"account":{"id":"795442","username":"chrismo","acct":"chrismo@ruby.social","display_name":"chrismo","locked":false,"bot":false,"discoverable":true,"group":false,"created_at":"2019-04-25T00:00:00.000Z","note":"<p>i mash keys</p>","url":"https://ruby.social/@chrismo","avatar":"https://files.mastodon.social/cache/accounts/avatars/000/795/442/original/12084217a7eb7513.png","avatar_static":"https://files.mastodon.social/cache/accounts/avatars/000/795/442/original/12084217a7eb7513.png","header":"https://static-cdn.mastodon.social/headers/original/missing.png","header_static":"https://static-cdn.mastodon.social/headers/original/missing.png","followers_count":40,"following_count":62,"statuses_count":42,"last_status_at":"2022-11-15","emojis":[],"fields":[{"name":"web","value":"clabs.org","verified_at":null},{"name":"github","value":"github.com/chrismo","verified_at":null},{"name":"twitter","value":"twitter.com/the_chrismo","verified_at":null},{"name":"bandcamp","value":"cstudios.bandcamp.com","verified_at":null}]},"media_attachments":[],"mentions":[],"tags":[{"name":"MUSICTUESDAY","url":"https://mastodon.social/tags/MUSICTUESDAY"},{"name":"piano","url":"https://mastodon.social/tags/piano"},{"name":"neoclassical","url":"https://mastodon.social/tags/neoclassical"},{"name":"jazz","url":"https://mastodon.social/tags/jazz"}],"emojis":[],"card":null,"poll":null}
            
            """;
        var reader = new BufferedReader(new StringReader(stream));
        var list = new ArrayList<>();
        while (true) {
            Event event = Event.read(reader);
            if (event == null) break;
            list.add(event);
        }
        assertEquals(3, list.size());
    }
}