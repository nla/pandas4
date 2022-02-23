package pandas.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pandas.collection.TitleEditForm;
import pandas.collection.TitleService;
import pandas.gather.GatherMethodRepository;

import java.util.List;

@SpringBootTest(properties = {
        "spring.datasource.url = jdbc:h2:mem:it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.properties.hibernate.dialect = pandas.core.PandasH2Dialect"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TitleTest {
    @Autowired private TitleService titleService;
    @Autowired private GatherMethodRepository gatherMethodRepository;

    @Test
    public void test() {
        TitleEditForm form = titleService.newTitleForm(List.of(), List.of());
        form.setName("Test title");
        form.setGatherMethod(gatherMethodRepository.findByName("Heritrix").orElseThrow());
        form.setTitleUrl("http://example.com/");
        var title = titleService.save(form, null);
        System.out.println(title.getTep().getId());
    }
}
