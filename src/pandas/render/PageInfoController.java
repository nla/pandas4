package pandas.render;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageInfoController {
    @GetMapping(value = "/pageinfo", produces = "application/json")
    @ResponseBody
    public PageInfo load(@RequestParam(name = "url") String url) throws Exception {
        return PageInfo.fetch(url);
    }
}
