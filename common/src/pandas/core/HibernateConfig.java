package pandas.core;

import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.jboss.jandex.IndexView;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {
    @Bean
    public HibernatePropertiesCustomizer metadataContributor() {
        return hibernateProperties -> hibernateProperties.put(
                "hibernate.metadata_contributor",
                new PandasMetadataContributor()
        );
    }

    private class PandasMetadataContributor implements MetadataContributor {
        @Override
        public void contribute(InFlightMetadataCollector metadataCollector, IndexView jandexIndex) {

        }
    }
}
