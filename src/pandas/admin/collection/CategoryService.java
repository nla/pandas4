package pandas.admin.collection;

import org.apache.maven.model.Site;
import org.hibernate.search.jpa.Search;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CategoryService {
    private final SubjectRepository subjectRepository;
    private final CollectionRepository collectionRepository;
    private final Category topLevelCategory;
    private final EntityManager entityManager;

    public CategoryService(SubjectRepository subjectRepository, CollectionRepository collectionRepository, EntityManager entityManager) {
        this.subjectRepository = subjectRepository;
        this.collectionRepository = collectionRepository;
        topLevelCategory = new Category() {
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
                return new ArrayList<>(subjectRepository.findByParentIsNullOrderByName());
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

    public List<Breadcrumb> breadcrumbs(Category category) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        Category next = category;
        while (next != null) {
            Category c = next;
            breadcrumbs.add(new Breadcrumb() {
                @Override
                public String getHref() {
                    return "collections/" + c.getCategoryId();
                }

                @Override
                public String getName() {
                    return c.getName();
                }
            });
            next = next.getParentCategory();
        }
        if (category.getCategoryId() != 0) {
            breadcrumbs.add(new Breadcrumb() {
                @Override
                public String getHref() {
                    return "collections/" + topLevelCategory.getCategoryId();
                }

                @Override
                public String getName() {
                    return topLevelCategory.getName();
                }
            });
        }
        Collections.reverse(breadcrumbs);
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
}
