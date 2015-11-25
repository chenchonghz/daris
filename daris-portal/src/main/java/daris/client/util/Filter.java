package daris.client.util;

public interface Filter<T> {

    boolean matches(T o);
}
