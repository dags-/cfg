package me.dags.config;

import me.dags.config.style.Comment;
import me.dags.config.style.Order;
import me.dags.config.style.Style;

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
    private final Style style;

    private ObjectNode() {
        comments = Collections.emptyMap();
        fields = Collections.emptyMap();
        order = Collections.emptyList();
        type = null;
        field = null;
        style = Style.DEFAULT;
    }

    private ObjectNode(Builder<T> builder) {
        comments = Collections.unmodifiableMap(new HashMap<>(builder.comments));
        fields = Collections.unmodifiableMap(new HashMap<>(builder.fields));
        order = Collections.unmodifiableList(new LinkedList<>(builder.order));
        type = builder.type;
        field = builder.field;
        style = builder.style;
    }

    @Override
    public void write(Appendable appendable, Object owner, Style style, int level, boolean key) throws IOException, IllegalAccessException {
        boolean root = level == 0;
        boolean empty;

        if (root || this.style.override()) {
            style = this.style;
        }

        if (!root) {
            Render.startObject(appendable);
        }

        empty = !Render.header(appendable, comments.get("#header"), style, level, root);

        int childLevel = level + 1;
        Object instance = field != null ? get(owner) : owner;
        Collection<String> keys = order.isEmpty() ? fields.keySet() : order;

        for (String next : keys) {
            Node value = fields.get(next);

            if (value == null || style.ignoreEmpty() && value.isEmpty(instance)) {
                continue;
            }

            if (empty) {
                if (!root) {
                    Render.lineEnd(appendable);
                }
            } else {
                Render.lineEnd(appendable);
                Render.lineBreaks(appendable, style);
            }

            empty = false;
            Render.comment(appendable, comments.get(next), style, level);
            Render.indents(appendable, style, level);
            Render.key(appendable, MapNode.getSafeKey(next), style);
            value.write(appendable, instance, style, childLevel, false);
        }

        if (!root) {
            if (empty) {
                Render.endObject(appendable);
            } else {
                Render.lineEnd(appendable);
                Render.indents(appendable, style, level - 1);
                Render.endObject(appendable);
            }
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

    static <T> Builder<T> builder(Class<T> type, Field field) {
        return new Builder<>(type, field);
    }

    static class Builder<T> {

        private final Map<String, Comment> comments = new HashMap<>();
        private final Map<String, Node> fields = new HashMap<>();
        private final Set<String> order = new LinkedHashSet<>();
        private final Class<T> type;
        private final Field field;
        private Style style = Style.DEFAULT;

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

        Builder style(Style style) {
            this.style = style;
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
