package pandas.search;

import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.*;
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
    public SearchPredicate predicate(SearchPredicateFactory f, MultiValueMap<String, String> form,
                                     boolean not) {
        LocalDate start = parseDate(form.getFirst(param + ".start"));
        LocalDate end = parseDate(form.getFirst(param + ".end"));
        boolean never = "on".equals(form.getFirst(param + ".never"));
        if (start != null || end != null) {
            Instant lowerBound = start == null ? null : start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            Instant upperBound = end == null ? null : end.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            var isWithinDateRange = f.range().field(field).between(lowerBound, INCLUDED, upperBound, EXCLUDED);
            if (never) {
                var hasNoValue = f.matchAll().except(f.exists().field(field));
                return f.or(isWithinDateRange, hasNoValue).toPredicate();
            } else {
                return isWithinDateRange.toPredicate();
            }
        } else if (never) {
            return f.not(f.exists().field(field)).toPredicate();
        }
        return f.matchAll().toPredicate();
    }

    @Override
    public SearchPredicate searchPredicate(SearchPredicateFactory f, MultiValueMap<String, String> queryParams) {
        return f.matchAll().toPredicate();
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
