package daris.client.util;

import java.util.List;
import java.util.Map;

import arc.mf.client.util.ObjectUtil;

public class CollectionUtil {

    public static <K, V> boolean mapEquals(Map<K, V> map1, Map<K, V> map2) {
        if (map1 == null && map2 == null) {
            return true;
        }
        if (map1 == null && map2 != null) {
            return false;
        }
        if (map1 != null && map2 == null) {
            return false;
        }
        if (map1.size() != map2.size()) {
            return false;
        }
        for (K key : map1.keySet()) {
            if (!ObjectUtil.equals(map1.get(key), map2.get(key))) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean listEquals(List<T> list1, List<T> list2) {
        if (list1 == null && list2 == null) {
            return true;
        }
        if (list1 == null && list2 != null) {
            return false;
        }
        if (list1 != null && list2 == null) {
            return false;
        }
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!ObjectUtil.equals(list1.get(i), list2.get(i))) {
                return false;
            }
        }
        return true;
    }
}
