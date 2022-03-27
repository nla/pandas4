package pandas;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pandas.agency.Agency;
import pandas.agency.AgencyRepository;
import pandas.agency.User;
import pandas.agency.UserRepository;
import pandas.collection.Format;
import pandas.collection.FormatRepository;
import pandas.collection.Status;
import pandas.collection.StatusRepository;
import pandas.core.Organisation;
import pandas.core.Role;
import pandas.gather.*;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
public abstract class IntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
}
