package pandas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "pandas")
public class PandasGatherer {
    public static void main(String[] args) {
        SpringApplication.run(PandasGatherer.class, args);
    }
}
