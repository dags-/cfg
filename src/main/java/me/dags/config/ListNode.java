package me.dags.config;

import me.dags.config.style.Style;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Iterator;
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
            this.valueTemplate = ClassMapper.getFactory(childType);
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
    public void write(Appendable appendable, Object owner, Style style, int level, boolean key) throws IOException, IllegalAccessException {
        boolean root = level == 0;
        boolean empty = true;

        if (!root) {
            Render.startList(appendable);
        }

        int childLevel = level + 1;
        List<?> list = (List<?>) get(owner);
        Iterator<?> iterator = list.iterator();

        while (iterator.hasNext()) {
            if (empty) {
                if (!root) {
                    Render.lineEnd(appendable);
                }
            }

            empty = false;
            Object next = iterator.next();
            Render.indents(appendable, style, level);
            valueTemplate.write(appendable, next, style, childLevel, false);

            if (iterator.hasNext()) {
                Render.lineEnd(appendable);
            }
        }

        if (!root) {
            if (empty) {
                Render.endList(appendable);
            } else {
                Render.lineEnd(appendable);
                Render.indents(appendable, style, level - 1);
                Render.endList(appendable);
            }
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
