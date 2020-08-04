package pandas.admin.collection;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.MustJunction;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import pandas.admin.Config;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
public class TitleController {
    private final TitleRepository titleRepository;
    private final SubjectRepository subjectRepository;
    private final Config config;
    private final EntityManager entityManager;

    public TitleController(TitleRepository titleRepository, SubjectRepository subjectRepository, Config config, EntityManager entityManager) {
        this.titleRepository = titleRepository;
        this.subjectRepository = subjectRepository;
        this.config = config;
        this.entityManager = entityManager;
    }

    @GetMapping("/titles/{id}")
    public RedirectView get(@PathVariable("id") long id) {
        return new RedirectView(config.managementDirectActionUrl("titleView?id=" + id));
    }

    @GetMapping("/titles")
    public String search(@RequestParam(name = "q", required = false) String q,
                         @RequestParam(name = "subject", defaultValue = "") Set<Long> selectedSubjectIds,
                         @PageableDefault(40) Pageable pageable,
                         Model model) {

        var uriBuilder = UriComponentsBuilder.fromPath("titles");
        var em = Search.getFullTextEntityManager(entityManager);
        var qb = em.getSearchFactory().buildQueryBuilder().forEntity(Title.class).get();

        var bool = qb.bool().must(qb.all().createQuery());
        if (q != null && !q.isBlank()) {
            bool.must(qb.simpleQueryString().onField("name").withAndAsDefaultOperator().matching(q).createQuery());
            uriBuilder.queryParam("q", q);
        }

        if (!selectedSubjectIds.isEmpty()) {
            BooleanJunction<?> clauses = qb.bool();
            for (Long subjectId : selectedSubjectIds) {
                clauses = clauses.should(qb.keyword().onField("subjects.id").matching(subjectId).createQuery());
            }
            bool.must(clauses.createQuery());
            uriBuilder.queryParam("subject", selectedSubjectIds);
        }

        var jpaQuery = em.createFullTextQuery(bool.createQuery(), Title.class);
        jpaQuery.setFirstResult((int)pageable.getOffset());
        jpaQuery.setMaxResults(pageable.getPageSize());
        PageImpl<Title> page = new PageImpl<Title>(jpaQuery.getResultList(), pageable, jpaQuery.getResultSize());

        model.addAttribute("results", page);
        model.addAttribute("q", q);
        model.addAttribute("uriBuilder", uriBuilder);
        model.addAttribute("allSubjects", subjectRepository.findAllByOrderByName());
        model.addAttribute("selectedSubjectIds", selectedSubjectIds);
        return "TitleSearch";
    }

    @GetMapping("/titles/reindex")
    @ResponseBody
    public String reindex() throws InterruptedException {
        Search.getFullTextEntityManager(entityManager).createIndexer(Title.class).startAndWait();
        return "ok";
    }

    @PostMapping("/titles/datatable")
    @JsonView(DataTablesOutput.View.class)
    @ResponseBody
    public DataTablesOutput<Title> table(@Valid @RequestBody DataTablesInput input) {
        return titleRepository.findAll(input);
    }
}
