package pandas.core;

import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class HtmlSanitizerControllerAdvice {
    private final PolicyFactory htmlSanitizer;

    public HtmlSanitizerControllerAdvice(@Qualifier("htmlSanitizer") PolicyFactory htmlSanitizer) {
        this.htmlSanitizer = htmlSanitizer;
    }

    @ModelAttribute("htmlSanitizer")
    public PolicyFactory htmlSanitizer() {
        return htmlSanitizer;
    }
}
