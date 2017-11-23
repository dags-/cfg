package me.dags.config;

import me.dags.config.style.Comment;
import me.dags.config.style.Default;
import me.dags.config.style.Order;
import me.dags.config.style.Style;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
final class ClassMapper {

    private ClassMapper() {

    }

    static <T> Node<T> getFactory(Class<T> type) {
        return createFactory(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> Node<T> createFactory(Class<T> type) {
        if (isPrimitive(type)) {
            return new ValueNode(null, getParser(type), null);
        }
        return createObjectFactory(type, null);
    }

    private static <T> Node<T> createObjectFactory(Class<T> c, Field parent) {
        ObjectNode.Builder<T> builder = ObjectNode.builder(c, parent);

        if (c.isAnnotationPresent(Style.class)) {
            builder.style(c.getAnnotation(Style.class));
        }

        if (c.isAnnotationPresent(Order.class)) {
            Order order = c.getAnnotation(Order.class);
            builder.order(order);
        }

        if (c.isAnnotationPresent(Comment.class)) {
            Comment comment = c.getAnnotation(Comment.class);
            builder.header(comment);
        }

        for (Field field : c.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (isValidModifier(modifiers)) {
                field.setAccessible(true);
                builder.field(field.getName(), createFactory(field));
                if (field.isAnnotationPresent(Comment.class)) {
                    Comment comment = field.getAnnotation(Comment.class);
                    builder.comment(field.getName(), comment);
                }
            }
        }

        return builder.build();
    }

    private static Node createFactory(Field field) {
        Class<?> type = field.getType();
        if (ClassMapper.isPrimitive(type)) {
            String def = null;
            if (field.isAnnotationPresent(Default.class)) {
                def = field.getAnnotation(Default.class).value();
            }
            return new ValueNode(field, getParser(type), def);
        }

        if (Map.class.isAssignableFrom(type)) {
            return new MapNode(field);
        }

        if (List.class.isAssignableFrom(type)) {
            return new ListNode(field);
        }

        return createObjectFactory(field.getType(), field);
    }

    private static boolean isValidModifier(int modifier) {
        return !Modifier.isStatic(modifier) && !Modifier.isTransient(modifier);
    }

    private static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || type.isEnum() || PRIMITIVES.containsKey(type);
    }

    private static Function<String, ?> getParser(Class<?> type) {
        if (type.isEnum()) {
            return enumParser(type);
        }
        return PRIMITIVES.get(type);
    }

    private static final Map<Class<?>, Function<String, ?>> PRIMITIVES = new HashMap<Class<?>, Function<String, ?>>() {{
        put(boolean.class, Boolean::parseBoolean);
        put(Boolean.class, Boolean::parseBoolean);
        put(byte.class, Byte::parseByte);
        put(Byte.class, Byte::parseByte);
        put(char.class, Byte::parseByte);
        put(Character.class, Byte::parseByte);
        put(double.class, Double::parseDouble);
        put(Double.class, Double::parseDouble);
        put(float.class, Float::parseFloat);
        put(Float.class, Float::parseFloat);
        put(int.class, Integer::parseInt);
        put(Integer.class, Integer::parseInt);
        put(long.class, Long::parseLong);
        put(Long.class, Long::parseLong);
        put(short.class, Short::parseShort);
        put(Short.class, Short::parseShort);
        put(String.class, s -> s);
    }};

    private static Function<String, ?> enumParser(Class<?> type) {
        final Object[] values = type.getEnumConstants();
        return s -> {
            for (Object o : values) {
                if (o.toString().equalsIgnoreCase(s)) {
                    return o;
                }
            }
            return null;
        };
    }
}
