package pandas.delivery.util;

import java.util.*;

/**
 * A set which keeps track of the number of times each value was added to it. It can then later produce a list ordered
 * by frequency.
 */
public class CountingSet<T> {
    private Map<T,Long> map = new HashMap<>();

    public void add(T value) {
        if (value == null) return;
        Long count = map.get(value);
        map.put(value, count == null ? 1 : count + 1);
    }

    /**
     * Returns a list of the set values ordered in decreasing order of frequency.
     */
    public List<T> listByFrequencyDecreasing() {
        var list = new ArrayList<>(map.keySet());
        list.sort(Comparator.comparing(value -> map.get(value)).reversed());
        return list;
    }
}
