package me.dags.config;

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
    public void write(Object owner, Appendable appendable, String parentIndent, String indent) throws IOException, IllegalAccessException {
        if (field == null) {
            appendable.append(owner.toString());
        } else {
            appendable.append(get(owner).toString());
        }
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
