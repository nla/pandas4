package pandas.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest(properties = "spring.jpa.mapping-resources=pandas/orm-mysql.xml")
class MysqlOrmMappingTest {
    @Test
    void mappingLoads() {
    }
}
