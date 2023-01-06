package pandas.discovery;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
public class BookmarkletController {
    @GetMapping(value = "/bookmarklet")
    public void get(Model model, HttpServletRequest request) throws IOException {
        try (InputStream stream = BookmarkletController.class.getResourceAsStream("/bookmarklets/bookmarklet.js")) {
            String bookmarklet = new String(stream.readAllBytes(), UTF_8);
            String baseUrl = ServletUriComponentsBuilder.fromContextPath(request).build().toString().replaceFirst("/$", "");
            bookmarklet = bookmarklet.replace("https://pandas.nla.gov.au/admin", baseUrl);
            bookmarklet = bookmarklet.replace("\n", "").replaceAll("  +", " ");
            model.addAttribute("bookmarklet", "javascript:" + UriUtils.encode(bookmarklet, UTF_8));
        }
    }
}
