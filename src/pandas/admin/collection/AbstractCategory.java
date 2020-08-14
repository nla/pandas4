package pandas.admin.collection;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

public abstract class AbstractCategory implements Category {
    private static final PolicyFactory htmlSanitizer = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS).and(Sanitizers.LINKS).and(Sanitizers.TABLES);

    @Override
    public String getFullName() {
        return getBreadcrumbs().stream().map(Category::getName).collect(joining(" / "));
    }

    @Override
    public List<Category> getBreadcrumbs() {
        List<Category> breadcrumbs = new ArrayList<>();
        for (Category c = this; c != null; c = c.getParentCategory()) {
            breadcrumbs.add(c);
        }
        Collections.reverse(breadcrumbs);
        return breadcrumbs;
    }

    @Override
    public String getDescriptionSanitized() {
        String description = getDescription();
        return description == null ? null : htmlSanitizer.sanitize(description);
    }

    @Override
    public boolean isDisplayed() {
        return true;
    }
}
