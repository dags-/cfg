package me.dags.config;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
class ListNode implements Node {

    static final ListNode EMPTY = new ListNode(null);

    private final Field field;
    private final Node valueTemplate;
    private final Constructor<?> valueConstructor;

    ListNode(Field field) {
        if (field == null) {
            this.field = null;
            this.valueTemplate = null;
            this.valueConstructor = null;
        } else {
            Type type = field.getGenericType();
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            Class<?> childType = (Class<?>) args[0];
            this.field = field;
            this.valueTemplate = ClassMapper.getFactory(childType).toInternal();
            Constructor<?> constructor;
            try {
                constructor = childType.getConstructor();
            } catch (NoSuchMethodException e) {
                constructor = null;
            }
            this.valueConstructor = constructor;
        }
    }

    @Override
    public void write(Object owner, Appendable appendable, String parentIndent, String indent) throws IOException, IllegalAccessException {
        if (indent.length() != 0) {
            appendable.append("[\n");
        }

        String childIndent = indent + " ";
        List<?> list = (List<?>) get(owner);
        for (Object o : list) {
            appendable.append(indent);
            valueTemplate.write(o, appendable, indent, childIndent);
            appendable.append('\n');
        }

        if (indent.length() != 0) {
            appendable.append(parentIndent);
            appendable.append(']');
        }
    }

    @Override
    public Object newInstance() throws IllegalAccessException, InstantiationException {
        return new ArrayList<>();
    }

    @Override
    public Object get(Object owner) throws IllegalAccessException {
        return field.get(owner);
    }

    @Override
    public void set(Object owner, Object value) throws IllegalAccessException {
        field.set(owner, value);
    }

    @Override
    public ListNode asList() {
        return this;
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public boolean isEmpty(Object owner) throws IllegalAccessException {
        return ((List) get(owner)).isEmpty();
    }

    @Override
    public boolean isList() {
        return true;
    }

    Object newValueInstance() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        return valueConstructor == null ? null : valueConstructor.newInstance();
    }

    Node getValueTemplate() {
        return valueTemplate;
    }

    List getList(Object owner) throws IllegalAccessException {
        return (List) get(owner);
    }
}
