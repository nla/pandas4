package pandas.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends CrudRepository<Report, Long> {
    @Query("select r from Report r\n" +
            "where (:agencyId is null or r.owner.role.organisation.agency.id = :agencyId)\n" +
            "and (:ownerId is null or r.owner.id = :ownerId)\n" +
            "and r.lastGenerationDate is null\n" +
            "order by r.nextGenerationDate")
    Page<Report> worktrayRequested(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select r from Report r\n" +
            "where (:agencyId is null or r.owner.role.organisation.agency.id = :agencyId)\n" +
            "and (:ownerId is null or r.owner.id = :ownerId)\n" +
            "and r.isVisible <> false\n" +
            "and r.lastGenerationDate is not null\n" +
            "order by r.lastGenerationDate desc")
    Page<Report> worktrayGenerated(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);
}
