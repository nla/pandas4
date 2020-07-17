package pandas.admin.gather;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatherFilterPresetRepository extends PagingAndSortingRepository<GatherFilterPreset, Long> {
}
