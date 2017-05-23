package daris.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class CollectionUtils {

    @SafeVarargs
    public static <T> Map<T, T> createMap(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Array size must be even number.");
        }
        Map<T, T> map = new LinkedHashMap<T, T>();
        for (int i = 0; i < array.length; i += 2) {
            map.put(array[i], array[i + 1]);
        }
        return map;
    }

}
