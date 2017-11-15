package me.dags.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
public interface MapperFactory<T> {

    default Node<T> toInternal() {
        return (Node<T>) this;
    }

    default T read(InputStream inputStream) throws Exception {
        Node<T> internal = toInternal();
        if (internal.isValue()) {
            try (Parser parser = new Parser(inputStream)) {
                return parser.unmarshal(null, internal);
            }
        }

        if (internal.isObject()) {
            T t = internal.newInstance();
            try (Parser parser = new Parser(inputStream)) {
                parser.unmarshal(t, internal);
            }
            return t;
        }

        throw new IllegalStateException("Cannot read node type: " + internal.getClass());
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

    default void write(Object object, Writer writer) throws Exception {
        toInternal().write(object, writer, "", "");
    }

    default void write(Object object, OutputStream outputStream) throws Exception {
        try (Writer writer = new OutputStreamWriter(outputStream)) {
            write(object, writer);
        }
    }

    default void write(Object object, File file) throws Exception {
        file.getAbsoluteFile().getParentFile().mkdirs();
        try (Writer writer = new FileWriter(file)) {
            write(object, writer);
        }
    }

    default void write(Object object, Path path) throws Exception {
        Files.createDirectories(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path)) {
            write(object, writer);
        }
    }

    static <T> MapperFactory<T> of(Class<T> type) {
        return ClassMapper.getFactory(type);
    }
}
