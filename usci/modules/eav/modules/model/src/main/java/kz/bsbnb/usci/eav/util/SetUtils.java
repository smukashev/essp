package kz.bsbnb.usci.eav.util;

import java.util.*;

public class SetUtils {
    private static <T> Set<T> union(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.addAll(setB);
        return tmp;
    }

    public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new HashSet<T>();
        for (T x : setA)
            if (setB.contains(x))
                tmp.add(x);
        return tmp;
    }

    public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
        Set<T> tmp = new HashSet<T>();
        for (T key : setA) {
            if (!setB.contains(key)) {
                tmp.add(key);
            }
        }
        return tmp;
    }

    public static <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
        Set<T> tmpA;
        Set<T> tmpB;

        tmpA = union(setA, setB);
        tmpB = intersection(setA, setB);
        return difference(tmpA, tmpB);
    }

    public static <T> boolean isSubset(Set<T> setA, Set<T> setB) {
        return setB.containsAll(setA);
    }

    public static <T> boolean isSuperset(Set<T> setA, Set<T> setB) {
        return setA.containsAll(setB);
    }

    public static <T> T getRandomElement(Collection<T> collection, Random random) {
        return new ArrayList<T>(collection).get(random.nextInt(collection.size()));
    }

    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

    public static <K, V> void putMapValue(Map<K, Set<V>> map, K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<V>());
        }
        map.get(key).add(value);
    }

}
