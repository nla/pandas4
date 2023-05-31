package pandas.social;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachmentArchiverStateRepository extends CrudRepository<AttachmentArchiverState, Long> {
    default AttachmentArchiverState findAny() {
        var iterator = findAll().iterator();
        if (iterator.hasNext()) return iterator.next();
        return null;
    }
}
