package pandas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
}
