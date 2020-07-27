package pandas.admin;

import org.hibernate.search.jpa.Search;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public interface CollectionRepository extends CrudRepository<Collection, Long> {
//    @PersistenceContext
//    private EntityManager entityManger;
//
//    @SuppressWarnings("unchecked")
//    @Transactional
//    public List<Collection> search(String q) {
//        var fullTextEntityManager = Search.getFullTextEntityManager(entityManger);
//        var qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Collection.class).get();
//        var query = qb.keyword().onField("name").matching(q).createQuery();
//        var jpaQuery = fullTextEntityManager.createFullTextQuery(query, Collection.class);
//        return jpaQuery.getResultList();
//    }
}
