package me.dags.config;

import me.dags.config.style.Style;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public interface Mapper<T> {

    default void setValue(T owner, Object value, String... path) {
        try {
            Node node = (Node<T>) this;
            Object parent = owner;
            for (int i = 0, end = path.length; i < end; i++) {
                if (node.isObject()) {
                    if (i > 0) {
                        parent = node.get(parent);
                    }
                    node = node.asObject().getChild(path[i]);
                } else {
                    return;
                }
            }
            if (node.isValue()) {
                node.set(parent, value);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    default Optional<?> getValue(T owner, String... path) {
        try {
            Node node = (Node<T>) this;
            Object parent = owner;
            for (int i = 0, end = path.length; i < end; i++) {
                if (node.isObject()) {
                    if (i > 0) {
                        parent = node.get(parent);
                    }
                    node = node.asObject().getChild(path[i]);
                } else {
                    return Optional.empty();
                }
            }

            if (node.isValue()) {
                return Optional.ofNullable(node.get(parent));
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    default T read(Reader reader) throws Exception {
        Node<T> internal = (Node<T>) this;

        if (internal.isValue()) {
            try (Parser parser = new Parser(reader)) {
                return parser.unMarshal(null, internal);
            }
        }

        if (internal.isObject()) {
            T t = internal.newInstance();
            try (Parser parser = new Parser(reader)) {
                parser.unMarshal(t, internal);
            }
            return t;
        }

        throw new IllegalStateException("Cannot read node type: " + internal.getClass());
    }

    default T read(InputStream inputStream) throws Exception {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return read(reader);
        }
    }

    default T read(File file) throws Exception {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return read(inputStream);
        }
    }

    default T read(Path path) throws Exception {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return read(inputStream);
        }
    }

    /**
     * Performs a read and write on the given file, returning it's value
     * If the file does not exist on read, the fallback value is used
     */
    default T must(File file, Supplier<T> fallback) {
        T value;

        if (file.exists()) {
            try {
                value = read(file);
            } catch (Exception e) {
                e.printStackTrace();
                value = fallback.get();
            }
        } else {
            value = fallback.get();
        }

        try {
            write(value, file);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return value;
    }

    default T must(Path path, Supplier<T> fallback) {
        if (Files.exists(path)) {
            try {
                return read(path);
            } catch (Exception e) {
                e.printStackTrace();
                return fallback.get();
            }
        } else {
            T value = fallback.get();
            try {
                write(value, path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return value;
        }
    }

    default void write(T object, Writer writer) throws Exception {
        ((Node<T>) this).write(writer, object, Style.DEFAULT, 0, false);
    }

    default void write(T object, File file) throws Exception {
        file.getAbsoluteFile().getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            write(object, writer);
        }
    }

    default void write(T object, Path path) throws Exception {
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path)) {
            write(object, writer);
        }
    }

    static <T> Mapper<T> of(Class<T> type) {
        return ClassMapper.getFactory(type);
    }
}
