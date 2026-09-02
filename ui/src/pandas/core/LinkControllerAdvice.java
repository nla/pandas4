package pandas.core;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ControllerAdvice
public class LinkControllerAdvice {
    private final LinkProperties properties;

    public LinkControllerAdvice(LinkProperties properties) {
        this.properties = properties;
    }

    @ModelAttribute("link")
    public Link link(HttpServletRequest request) {
        String absoluteContextPath = ServletUriComponentsBuilder.fromContextPath(request).toUriString();
        return new Link(properties, request.getContextPath(), absoluteContextPath);
    }
}
