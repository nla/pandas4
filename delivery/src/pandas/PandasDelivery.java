package pandas;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import pandas.core.PandasBanner;

@SpringBootApplication(exclude = ElasticsearchRestClientAutoConfiguration.class)
@EnableScheduling
public class PandasDelivery {
    public static void main(String[] args) {
        var app = new SpringApplication(PandasDelivery.class);
        app.setBanner(new PandasBanner());
        app.run(args);
    }

    @Bean(name = "htmlSanitizer")
    public PolicyFactory htmlSanitizer() {
        return Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.LINKS).and(Sanitizers.TABLES);
    }
}
