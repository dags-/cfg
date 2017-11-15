package me.dags.config;

import me.dags.config.annotation.Comment;
import me.dags.config.annotation.Order;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
class ObjectNode<T> implements Node<T> {

    static final ObjectNode EMPTY = new ObjectNode();

    private final Map<String, Comment> comments;
    private final Map<String, Node> fields;
    private final List<String> order;
    private final Class<T> type;
    private final Field field;
    private final boolean compact;

    private ObjectNode() {
        comments = Collections.emptyMap();
        fields = Collections.emptyMap();
        order = Collections.emptyList();
        type = null;
        field = null;
        compact = false;
    }

    private ObjectNode(Builder<T> builder) {
        comments = Collections.unmodifiableMap(new HashMap<>(builder.comments));
        fields = Collections.unmodifiableMap(new HashMap<>(builder.fields));
        order = Collections.unmodifiableList(new LinkedList<>(builder.order));
        type = builder.type;
        field = builder.field;
        compact = builder.compact;
    }

    @Override
    public void write(Object owner, Appendable appendable, String parentIndent, String indent) throws IOException, IllegalAccessException {
        if (indent.length() != 0) {
            appendable.append("{\n");
        }

        writeComment(appendable, "#header", indent);

        String childIndent = indent + " ";
        Object instance = field != null ? get(owner) : owner;

        Collection<String> keys = order.isEmpty() ? fields.keySet() : order;
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Node value = fields.get(key);
            if (value == null || value.isEmpty(instance)) {
                continue;
            }

            writeComment(appendable, key, indent);
            appendable.append(indent);
            appendable.append(key);
            appendable.append(": ");
            value.write(instance, appendable, indent, childIndent);

            if (iterator.hasNext()) {
                appendable.append('\n');
                if (!compact) {
                    appendable.append('\n');
                }
            }
        }

        if (indent.length() != 0) {
            appendable.append('\n');
            appendable.append(parentIndent);
            appendable.append('}');
        }
    }

    @Override
    public T newInstance() throws IllegalAccessException, InstantiationException {
        return type.newInstance();
    }

    @Override
    public Object get(Object owner) throws IllegalAccessException {
        if (field == null) {
            return null;
        }
        return field.get(owner);
    }

    @Override
    public void set(Object owner, Object value) throws IllegalAccessException {
        if (field != null) {
            field.set(owner, value);
        }
    }

    @Override
    public ObjectNode asObject() {
        return this;
    }

    @Override
    public boolean isPresent() {
        return this != EMPTY;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    Node getChild(String name) {
        return fields.getOrDefault(name, ValueNode.EMPTY);
    }

    private void writeComment(Appendable appendable, String name, String indent) throws IOException {
        if (compact) {
            return;
        }

        Comment comment = comments.get(name);
        if (comment != null) {
            for (String line : comment.value()) {
                appendable.append(indent);
                appendable.append("# ");
                appendable.append(line);
                appendable.append('\n');
            }
            if (name.equals("#header")) {
                appendable.append('\n');
            }
        }
    }

    static <T> Builder<T> builder(Class<T> type, Field field) {
        return new Builder<>(type, field);
    }

    static class Builder<T> {

        private final Map<String, Comment> comments = new HashMap<>();
        private final Map<String, Node> fields = new HashMap<>();
        private final Set<String> order = new LinkedHashSet<>();
        private final Class<T> type;
        private final Field field;
        private boolean compact = false;

        private Builder(Class<T> type, Field field) {
            this.type = type;
            this.field = field;
        }

        Builder order(Order order) {
            List<String> current = new ArrayList<>(this.order);
            this.order.clear();
            Collections.addAll(this.order, order.value());
            this.order.addAll(current);
            return this;
        }

        Builder compact() {
            this.compact = true;
            return this;
        }

        Builder header(Comment comment) {
            this.comments.put("#header", comment);
            return this;
        }

        Builder field(String name, Node template) {
            this.fields.put(name, template);
            this.order.add(name);
            return this;
        }

        Builder comment(String name, Comment comment) {
            comments.put(name, comment);
            return this;
        }

        ObjectNode<T> build() {
            return new ObjectNode<>(this);
        }
    }
}
