package me.dags.config;

import me.dags.config.style.Style;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class MapNode implements Node {

    static final MapNode EMPTY = new MapNode();

    private final Field field;
    private final Node keyTemplate;
    private final Node valueTemplate;
    private final Constructor<?> constructor;
    private final Constructor<?> keyConstructor;
    private final Constructor<?> valueConstructor;

    private MapNode() {
        this.field = null;
        this.keyTemplate = null;
        this.valueTemplate = null;
        this.constructor = null;
        this.keyConstructor = null;
        this.valueConstructor = null;
    }

    MapNode(Field field) {
        Type[] args = ClassUtils.getParamTypes(field);
        Class<?> keyType = (Class<?>) args[0];
        Class<?> valType = (Class<?>) args[1];

        this.field = field;
        this.keyTemplate = ClassMapper.getNode(keyType);
        this.valueTemplate = ClassMapper.getNode(valType);

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

        this.constructor = ClassUtils.getConstructor(field, LinkedHashMap.class);
        this.keyConstructor = keyCon;
        this.valueConstructor = valCon;
    }

    @Override
    public void write(Appendable appendable, Object owner, Style style, int level, boolean key) throws IOException, IllegalAccessException {
        boolean root = level == 0;
        boolean empty = true;

        if (!root) {
            Render.startObject(appendable);
        }

        int childLevel = level + 1;
        Map<?, ?> map = (Map) get(owner);
        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();

        if (!root && iterator.hasNext()) {
            Render.lineEnd(appendable);
        }

        while (iterator.hasNext()) {
            empty = false;
            Map.Entry<?, ?> field = iterator.next();
            Render.indents(appendable, style, level);
            keyTemplate.write(appendable, field.getKey(), style, childLevel, true);
            Render.assign(appendable, style);
            valueTemplate.write(appendable, field.getValue(), style, childLevel, false);

            if (iterator.hasNext()) {
                Render.lineEnd(appendable);
            }
        }

        if (!root) {
            if (empty) {
                appendable.append(Render.END_OBJECT);
            } else {
                appendable.append(Render.NEWLINE);
                Render.indents(appendable, style, level - 1);
                appendable.append(Render.END_OBJECT);
            }
        }
    }

    @Override
    public Object newInstance() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        return constructor.newInstance();
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

    Map<? ,?> getMap(Object owner) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Object value = field == null ? newInstance() : get(owner);
        return (Map) value;
    }

    static Object getSafeKey(Object value) {
        if (value instanceof String) {
            String text = (String) value;
            char box = (char) -1;

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == Render.NEWLINE) {
                    box = Render.ESCAPE;
                    break;
                }
                if (c == Render.ASSIGN) {
                    box = Render.QUOTE;
                }
            }

            if (box != (char) -1) {
                return box + text + box;
            }
        }
        return value;
    }

    static Object getSafeValue(Object value) {
        if (value instanceof String) {
            String text = (String) value;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == Render.NEWLINE) {
                    return Render.ESCAPE + text + Render.ESCAPE;
                }
            }
        }
        return value;
    }

    private static Class<?> constructor(Field field) {
        return field == null ? LinkedList.class : field.getType();
    }
}
