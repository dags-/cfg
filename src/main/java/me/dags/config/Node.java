package me.dags.config;

import me.dags.config.style.Style;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface Node<T> extends Mapper<T> {

    T newInstance() throws IllegalAccessException, InstantiationException;

    Object get(Object owner) throws IllegalAccessException, InstantiationException;

    void set(Object owner, Object value) throws IllegalAccessException;

    void write(Appendable appendable, Object owner, Style style, int level, boolean key) throws IOException, IllegalAccessException;

    default ObjectNode asObject() {
        return ObjectNode.EMPTY;
    }

    default MapNode asMap() {
        return MapNode.EMPTY;
    }

    default ListNode asList() {
        return ListNode.EMPTY;
    }

    default ValueNode asValue() {
        return ValueNode.EMPTY;
    }

    default boolean isPresent() {
        return true;
    }

    default boolean isValue() {
        return false;
    }

    default boolean isObject() {
        return false;
    }

    default boolean isList() {
        return false;
    }

    default boolean isMap() {
        return false;
    }

    default boolean isEmpty(Object owner) throws IllegalAccessException {
        return false;
    }
}
