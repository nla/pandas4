package pandas.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PandasWebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Remove DelimitedStringToCollectionConverter and DelimitedStringToArrayConverter because they
        // will convert a form value like "publisherName=Jane, James & Bob" into ["Jane", "James & Bob"]
        // We always generate urls like ?collection=1&collection=2 never ?collection=1,2 so this is not
        // desirable and a common source of bugs. They also turn ?foo= into an empty list instead of
        // single element list which is also a source of bugs.
        registry.removeConvertible(String.class, java.util.Collection.class);
        registry.removeConvertible(String.class, Object[].class);
    }
}
