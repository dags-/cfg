package me.dags.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dags <dags@dags.me>
 */
class ClassUtils {

    static Constructor<?> getConstructor(Field field, Class<?> def) {
        if (field != null) {
            Constructor<?> constructor = getConstructor(field.getType());
            if (constructor != null) {
                return constructor;
            }
        }
        return getConstructor(def);
    }

    static <T> Constructor<T> getConstructor(Class<T> type) {
        try {
            Constructor<T> constructor = type.getConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    static Type[] getParamTypes(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            if (args.length == 2) {
                if (args[0] instanceof Class && args[1] instanceof Class) {
                    return args;
                }
            }
        }
        return getParamTypes(field.getType());
    }

    private static Type[] getParamTypes(Class<?> c) {
        AtomicReference<Class<?>>  reference = new AtomicReference<>();
        Map<Type, Type> mappings = new HashMap<>();
        populate(c, mappings, reference);

        Class<?> root = reference.get();
        if (root == null) {
            throw new IllegalArgumentException("Could not determine root generic interface");
        }

        Type[] params = root.getTypeParameters();
        Type[] types = new Class[params.length];
        for (int i = 0; i < types.length; i++) {
            Type t = mappings.get(params[i]);
            if (t == null) {
                throw new IllegalArgumentException("No param type for " + params[i]);
            }
            types[i] = t;
        }

        return types;
    }

    private static void populate(Class<?> c, Map<Type, Type> mappings, AtomicReference<Class<?>> root) {
        if (c == Object.class) {
            return;
        }

        Type type = c.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            root.set(c);
            Class<?> parent = c.getSuperclass();
            Type[] childArgs = ((ParameterizedType) type).getActualTypeArguments();
            Type[] parentParams = parent.getTypeParameters();
            for (int i = 0; i < childArgs.length; i++) {
                Type t = childArgs[i];
                t = mappings.getOrDefault(t, t);
                mappings.put(parentParams[i], t);
            }
        }

        populate(c.getSuperclass(), mappings, root);
    }
}
