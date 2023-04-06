package pandas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import pandas.social.SocialArchiver;

@SpringBootApplication(scanBasePackages = "pandas")
@ConditionalOnNotWebApplication
public class PandasSocialArchiver implements CommandLineRunner {
    private final SocialArchiver socialArchiver;

    public PandasSocialArchiver(SocialArchiver socialArchiver) {
        this.socialArchiver = socialArchiver;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(PandasSocialArchiver.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        socialArchiver.run();
    }
}
