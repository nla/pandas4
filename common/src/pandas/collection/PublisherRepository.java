package pandas.collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.core.Organisation;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublisherRepository extends CrudRepository<Publisher, Long>, PagingAndSortingRepository<Publisher, Long> {
    Optional<Publisher> findByOrganisation(Organisation organisation);

    @Query("""
            SELECT t.publisher AS publisher, COUNT(DISTINCT t) AS titleCount
            FROM Title t
            JOIN t.collections c
            JOIN t.instances i
            WHERE c = :collection AND i.state.id = 1
            GROUP BY t.publisher
            ORDER BY COUNT(DISTINCT t) DESC
            """)
    List<PublisherTitleCount> findArchivedPublisherTitleCountsByCollection(@Param("collection") Collection collection);

    @Query(value = """
            WITH RECURSIVE descendents(COL_ID, ROOT_COL_ID) AS (
                SELECT COL_ID, COL_ID
                FROM col
                WHERE COL_ID = :collectionId
                UNION ALL
                SELECT c.COL_ID, d.ROOT_COL_ID
                FROM col c
                JOIN descendents d ON c.COL_PARENT_ID = d.COL_ID
            ),
            valid_instances AS (
                SELECT DISTINCT i.INSTANCE_ID, i.TITLE_ID
                FROM descendents d
                JOIN title_col tc ON tc.COLLECTION_ID = d.COL_ID
                JOIN instance i ON tc.TITLE_ID = i.TITLE_ID
                WHERE i.CURRENT_STATE_ID = 1
            )
            SELECT
                t.PUBLISHER_ID AS publisherId,
                o.NAME as publisherName,
                pt.PUBLISHER_TYPE as publisherType,
                COUNT(DISTINCT t.TITLE_ID) AS titleCount
            FROM valid_instances vi
            JOIN title t ON t.TITLE_ID = vi.TITLE_ID
            JOIN publisher p ON p.PUBLISHER_ID = t.PUBLISHER_ID
            JOIN organisation o ON o.ORGANISATION_ID = p.ORGANISATION_ID
            JOIN publisher_type pt ON pt.PUBLISHER_TYPE_ID = p.PUBLISHER_TYPE_ID
            GROUP BY publisherId, publisherName, publisherType
            ORDER BY titleCount DESC
            """, nativeQuery = true)
    List<PublisherTitleCount> findArchivedPublisherTitleCountsByCollectionRecursive(@Param("collectionId") long collectionId);

    interface PublisherTitleCount {
        long getPublisherId();
        String getPublisherName();
        String getPublisherType();
        long getTitleCount();
    }
}
