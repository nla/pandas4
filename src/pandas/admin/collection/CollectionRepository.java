package pandas.admin.collection;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

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
