package me.dags.config;

import me.dags.config.style.Style;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
class ValueNode implements Node {

    static final ValueNode EMPTY = new ValueNode();

    private final Field field;
    private final Function<String, ?> parser;

    private ValueNode() {
        this.field = null;
        this.parser = null;
    }

    ValueNode(Field field, Function<String, ?> parser) {
        this.field = field;
        this.parser = parser;
    }

    @Override
    public void write(Appendable appendable, Object owner, Style style, int level, boolean key) throws IOException, IllegalAccessException {
        Object value = field == null ? owner : get(owner);
        Object safe = key ? MapNode.getSafeKey(value) : MapNode.getSafeValue(value);
        appendable.append(safe.toString());
    }

    @Override
    public ValueNode asValue() {
        return this;
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public boolean isEmpty(Object owner) throws IllegalAccessException {
        return owner == null || (field != null && get(owner) == null);
    }

    @Override
    public boolean isValue() {
        return true;
    }

    @Override
    public Object newInstance() throws IllegalAccessException, InstantiationException {
        return null;
    }

    @Override
    public Object get(Object owner) throws IllegalAccessException {
        return field != null ? field.get(owner) : null;
    }

    @Override
    public void set(Object owner, Object value) throws IllegalAccessException {
        if (field == null || owner == null) {
            return;
        }
        field.set(owner, value);
    }

    Object parse(String input) {
        return parser != null ? parser.apply(input) : null;
    }
}
