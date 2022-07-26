package pandas.search;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.hibernate.search.util.common.data.RangeBoundInclusion.EXCLUDED;
import static org.hibernate.search.util.common.data.RangeBoundInclusion.INCLUDED;

public class DateFacet extends Facet {
    public DateFacet(String name, String param, String field) {
        super(name, param, field);
    }

    private static LocalDate parseDate(String s) {
        return s == null || s.isBlank() ? null : LocalDate.parse(s);
    }

    @Override
    public void mustMatch(SearchPredicateFactory f, BooleanPredicateClausesStep<?> bool, MultiValueMap<String, String> form) {
        LocalDate start = parseDate(form.getFirst(param + ".start"));
        LocalDate end = parseDate(form.getFirst(param + ".end"));
        boolean never = "on".equals(form.getFirst(param + ".never"));
        if (start != null || end != null) {
            Instant lowerBound = start == null ? null : start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            Instant upperBound = end == null ? null : end.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            if (never) {
                var or = f.bool();
                or.should(f.range().field(field).between(lowerBound, INCLUDED, upperBound, EXCLUDED));
                or.should(f.matchAll().except(f.exists().field(field)));
                bool.must(or);
            } else {
                bool.must(f.range().field(field).between(lowerBound, INCLUDED, upperBound, EXCLUDED));
            }
        } else if (never) {
            bool.mustNot(f.exists().field(field));
        }
    }

    @Override
    public void search(SearchPredicateFactory predicateFactory, BooleanPredicateClausesStep<?> bool, MultiValueMap<String, String> queryParams) {
    }

    @Override
    public FacetResults results(MultiValueMap<String, String> form, SearchResult<?> result) {
        String start = form.getFirst(param + ".start");
        String end = form.getFirst(param + ".end");
        boolean never = "on".equals(form.getFirst(param + ".never"));
        boolean active = (start != null && !start.isBlank()) || (end != null && !end.isBlank()) || never;
        return new FacetResults(name, param, List.of(), active, false, null) {
            @Override
            public boolean isVisible() {
                return true;
            }

            @Override
            public String getType() {
                return "date";
            }

            public String getStart() {
                return start;
            }

            public String getEnd() {
                return end;
            }

            public boolean getNever() {
                return never;
            }
        };
    }
}
