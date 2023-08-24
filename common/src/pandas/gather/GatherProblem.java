package pandas.gather;

import java.util.ArrayList;
import java.util.List;

public enum GatherProblem {
    NO_PROBLEMS("No problems"),
    SIZE_WARNING("Size warning"),
    SEED_ERROR("Seed error"),
    ;
    private final String text;

    GatherProblem(String text) {
        this.text = text;
    }

    public static List<GatherProblem> findAllById(Iterable<Long> ids) {
        var problems = new ArrayList<GatherProblem>();
        for (var id : ids) {
            problems.add(GatherProblem.values()[(int)(long)id]);
        }
        return problems;
    };

    public String text() {
        return text;
    }

    public Long id() {
        return (long) ordinal();
    }
}
