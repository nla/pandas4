package pandas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Utils {
    public static <T> List<T> sortBy(Iterable<T> items, Function<T, String> key) {
        List<T> list = new ArrayList<>();
        for (T item: items) {
            list.add(item);
        }
        list.sort(Comparator.comparing(key));
        return list;
    }

    /**
     * Check if each object in a collection has an identical value for the given property and returns it if so. Otherwise
     * returns null.
     */
    public static <O,T> T getIfSame(Iterable<O> objects, Function<O,T> getter) {
        T first = null;
        List<String> x;
        for (O object: objects) {
            T value = getter.apply(object);
            if (value == null) {
                return null;
            } else if (first == null) {
                first = value;
            } else if (!Objects.equals(first, value)) {
                return null;
            }
        }
        return first;
    }
}
