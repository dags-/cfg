package me.dags.config;

import me.dags.config.annotation.Comment;
import me.dags.config.annotation.Compact;
import me.dags.config.annotation.Order;

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

    static <T> MapperFactory<T> getFactory(Class<T> type) {
        return createFactory(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> MapperFactory<T> createFactory(Class<T> type) {
        if (isPrimitive(type)) {
            return new ValueNode(null, getParser(type));
        }
        return createObjectFactory(type, null);
    }

    private static <T> MapperFactory<T> createObjectFactory(Class<T> c, Field parent) {
        ObjectNode.Builder<T> builder = ObjectNode.builder(c, parent);

        if (c.isAnnotationPresent(Compact.class)) {
            builder.compact();
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
                builder.field(field.getName(), createFactory(field).toInternal());
                if (field.isAnnotationPresent(Comment.class)) {
                    Comment comment = field.getAnnotation(Comment.class);
                    builder.comment(field.getName(), comment);
                }
            }
        }

        return builder.build();
    }

    private static MapperFactory createFactory(Field field) {
        Class<?> type = field.getType();
        if (ClassMapper.isPrimitive(type)) {
            return new ValueNode(field, getParser(type));
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
        return PRIMITIVES.containsKey(type);
    }

    private static Function<String, ?> getParser(Class<?> type) {
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
}
