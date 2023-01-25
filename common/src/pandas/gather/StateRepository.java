package pandas.gather;

import org.springframework.data.repository.CrudRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public interface StateRepository extends CrudRepository<State, Long> {
    Optional<State> findByName(String name);

    List<State> findByNameIn(List<String> names);

    default List<State> mustFindByName(String... names) {
        return mustFindByName(Arrays.asList(names));
    }

    default List<State> mustFindByName(List<String> names) {
        List<State> states = findByNameIn(names);
        if (states.size() != names.size()) {
            var missingNames = new HashSet<>(names);
            for (var state: states) {
                missingNames.remove(state.getName());
            }
            throw new IllegalStateException("Database is missing states: " + missingNames);
        }
        return states;
    }
}
