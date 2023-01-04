package pandas.gather;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatherFilterPresetRepository extends CrudRepository<GatherFilterPreset, Long>, PagingAndSortingRepository<GatherFilterPreset, Long> {
}
