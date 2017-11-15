package me.dags.config;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class MapNode implements Node {

    static final MapNode EMPTY = new MapNode();

    private final Field field;
    private final Node keyTemplate;
    private final Node valueTemplate;
    private final Constructor<?> keyConstructor;
    private final Constructor<?> valueConstructor;

    private MapNode() {
        this.field = null;
        this.keyTemplate = null;
        this.valueTemplate = null;
        this.keyConstructor = null;
        this.valueConstructor = null;
    }

    MapNode(Field field) {
        this.field = field;
        Type type = field.getGenericType();
        Type[] args = ((ParameterizedType) type).getActualTypeArguments();
        Class<?> keyType = (Class<?>) args[0];
        Class<?> valType = (Class<?>) args[1];
        this.keyTemplate = ClassMapper.getFactory(keyType).toInternal();
        this.valueTemplate = ClassMapper.getFactory(valType).toInternal();

        Constructor<?> keyCon;
        try {
            keyCon = keyType.getConstructor();
            keyCon.setAccessible(true);
        } catch (NoSuchMethodException e) {
            keyCon = null;
        }

        Constructor<?> valCon;
        try {
            valCon = valType.getConstructor();
            valCon.setAccessible(true);
        } catch (NoSuchMethodException e) {
            valCon = null;
        }

        this.keyConstructor = keyCon;
        this.valueConstructor = valCon;
    }

    @Override
    public void write(Object owner, Appendable appendable, String parentIndent, String indent) throws IOException, IllegalAccessException {
        Map<?, ?> map = (Map) get(owner);
        if (indent.length() != 0) {
            appendable.append("{\n");
        }

        String childIndent = indent + " ";
        for (Map.Entry<?, ?> field : map.entrySet()) {
            appendable.append(indent);
            keyTemplate.write(field.getKey(), appendable, indent, childIndent);
            appendable.append(": ");
            valueTemplate.write(field.getValue(), appendable, indent, childIndent);
            appendable.append('\n');
        }

        if (indent.length() != 0) {
            appendable.append(parentIndent);
            appendable.append('}');
        }
    }

    @Override
    public Object newInstance() throws IllegalAccessException, InstantiationException {
        return new LinkedHashMap<>();
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
    public boolean isMap() {
        return true;
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public boolean isEmpty(Object owner) throws IllegalAccessException {
        return ((Map) get(owner)).isEmpty();
    }

    @Override
    public MapNode asMap() {
        return this;
    }

    Object newKeyInstance() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        return keyConstructor == null ? null : keyConstructor.newInstance();
    }

    Object newValueInstance() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        return valueConstructor == null ? null : valueConstructor.newInstance();
    }

    Node getKeyTemplate() {
        return keyTemplate;
    }

    Node getValueTemplate() {
        return valueTemplate;
    }

    Map<? ,?> getMap(Object owner) throws IllegalAccessException, InstantiationException {
        Object value = field == null ? newInstance() : get(owner);
        return (Map) value;
    }
}
