package pandas.admin.collection;

import org.hibernate.search.jpa.Search;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final SubjectRepository subjectRepository;
    private final CollectionRepository collectionRepository;
    private final Category topLevelCategory;
    private final EntityManager entityManager;

    public CategoryService(SubjectRepository subjectRepository, CollectionRepository collectionRepository, EntityManager entityManager) {
        this.subjectRepository = subjectRepository;
        this.collectionRepository = collectionRepository;
        topLevelCategory = new AbstractCategory() {
            @Override
            public Long getCategoryId() {
                return 0L;
            }

            @Override
            public String getName() {
                return "Collections";
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public String getThumbnailUrl() {
                return null;
            }

            @Override
            public void setThumbnailUrl(String thumbnailUrl) {
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void setDescription(String description) {
            }

            @Override
            public List<Category> getSubcategories() {
                ArrayList<Category> categories = new ArrayList<>(subjectRepository.findByParentIsNullOrderByName());
                categories.addAll(collectionRepository.findByParentIsNullAndSubjectsIsEmpty());
                return categories;
            }

            @Override
            public List<Category> getParents() {
                return List.of();
            }

            @Override
            public Category getParentCategory() {
                return null;
            }

            @Override
            public void setParentCategory(Category parent) {
                throw new UnsupportedOperationException("Top-level category can't have parent");
            }

            @Override
            public List<Title> getTitles() {
                return List.of();
            }

            @Override
            public String getType() {
                return Subject.class.getSimpleName();
            }

            @Override
            public String getFullName() {
                return getName();
            }

            @Override
            public List<Category> getBreadcrumbs() {
                return new ArrayList<>();
            }
        };
        this.entityManager = entityManager;
    }

    public Category getCategory(long id) {
        if (id == 0) {
            return topLevelCategory;
        } else if (Subject.isInRange(id)) {
            return subjectRepository.findById(id - Subject.CATEGORY_ID_RANGE_START).orElseThrow();
        } else {
            return collectionRepository.findById(id).orElseThrow();
        }
    }

    public List<Category> breadcrumbs(Category category) {
        List<Category> breadcrumbs = category.getBreadcrumbs();
        breadcrumbs.add(0, topLevelCategory);
        return breadcrumbs;
    }

    public void save(Category category) {
        if (category instanceof Subject) {
            subjectRepository.save((Subject)category);
        } else if (category instanceof Collection) {
            collectionRepository.save((Collection)category);
        } else {
            throw new UnsupportedOperationException(category.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<Category> search(String q) {
        var fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        var qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Subject.class).get();
        var query = qb.simpleQueryString().onField("name").boostedTo(1.5f).andField("fullName")
                .withAndAsDefaultOperator().matching(q).createQuery();
        var jpaQuery = fullTextEntityManager.createFullTextQuery(query, Subject.class, Collection.class);
        jpaQuery.setMaxResults(100);
        return jpaQuery.getResultList();
    }

    public void reindex() throws InterruptedException {
        Search.getFullTextEntityManager(entityManager).createIndexer(Subject.class, Collection.class).startAndWait();
    }

    public void delete(long id) {
        if (Subject.isInRange(id)) {
            subjectRepository.deleteById(id - Subject.CATEGORY_ID_RANGE_START);
        } else {
            collectionRepository.deleteById(id);
        }
    }

    public List<Category> getAll(List<Long> ids) {
        List<Long> subjectIds = new ArrayList<>();
        List<Long> collectionIds = new ArrayList<>();
        for (Long id : ids) {
            if (Subject.isInRange(id)) {
                subjectIds.add(Subject.toSubjectId(id));
            } else {
                collectionIds.add(id);
            }
        }
        List<Category> categories = new ArrayList<>();
        subjectRepository.findAllById(subjectIds).forEach(categories::add);
        collectionRepository.findAllById(collectionIds).forEach(categories::add);
        return categories;
    }
}
