package pandas.admin.collection;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRepository extends DataTablesRepository<Title,Long> {
    Title findByPi(long pi);
}
