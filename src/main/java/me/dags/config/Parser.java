package me.dags.config;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class Parser implements Closeable {

    private static final int START_OBJECT = '{';
    private static final int END_OBJECT = '}';
    private static final int START_LIST = '[';
    private static final int END_LIST = ']';
    private static final int NEWLINE = '\n';
    private static final int COMMENT = '#';
    private static final int ASSIGN = ':';
    private static final int LINE_SEPARATOR = System.lineSeparator().charAt(0);

    private final InputStream source;
    private final InputStreamReader reader;
    private final char[] buffer = new char[128];

    private char c = (char) -1;
    private boolean drained = true;

    Parser(InputStream source) {
        this.source = source;
        this.reader = new InputStreamReader(source, StandardCharsets.UTF_8);
    }

    <T> T unmarshal(T owner, Node element) throws Exception {
        return (T) parse(owner, element, false);
    }

    private Object parse(Object owner, Node element, boolean key) throws Exception {
        if (element.isObject()) {
            return populateObject(owner, element.asObject());
        } else if (element.isMap()) {
            return populateMap(owner, element.asMap());
        } else if (element.isList()) {
            return populateList(owner, element.asList());
        } else {
            skipSpace();
            String input = nextString(key);
            ValueNode node = element.asValue();
            return node.parse(input);
        }
    }

    private Object populateObject(Object objectOwner, ObjectNode object) throws Exception {
        // skip header comments, padding & object start char
        skipComments();
        if (peek() == START_OBJECT) {
            consume();
        }

        while (next()) {
            skipSpace();

            // check if end of object
            if (peek() == END_OBJECT) {
                consume();
                break;
            }

            // skip field comments
            skipComments();

            // read next field name
            String key = nextString(true);
            Node child = object.getChild(key);

            // parse field
            if (child.isPresent()) {
                Object owner = child.isObject() ? child.get(objectOwner) : objectOwner;
                Object value = parse(owner, child, false);
                if (child.isValue() && value != null) {
                    child.set(owner, value);
                }
            }
        }

        return objectOwner;
    }

    @SuppressWarnings("unchecked")
    private Map populateMap(Object mapOwner, MapNode map) throws Exception {
        // skip padding & map start char
        skipSpace();
        if (peek() == START_OBJECT) {
            consume();
        }

        Map instance = map.getMap(mapOwner);
        Node keyElement = map.getKeyTemplate();
        Node valueElement = map.getValueTemplate();

        while (next()) {
            skipSpace();

            // check if end of map
            if (peek() == END_OBJECT) {
                consume();
                break;
            }

            // parse key, skip assign char ':'
            Object key = parse(map.newKeyInstance(), keyElement, true);
            if (peek() == ASSIGN) {
                consume();
            }

            // parse value & put in map
            Object value = parse(map.newValueInstance(), valueElement, false);
            instance.put(key, value);
        }

        return instance;
    }

    @SuppressWarnings("unchecked")
    private List populateList(Object owner, ListNode list) throws Exception {
        // skip padding and start list char
        skipSpace();
        if (peek() == START_LIST) {
            consume();
        }

        List instance = list.getList(owner);
        Node node = list.getValueTemplate();
        while (next()) {
            skipSpace();

            // check if end of array
            if (peek() == END_LIST) {
                consume();
                break;
            }

            // parse value
            Object value = parse(list.newValueInstance(), node, false);
            instance.add(value);
        }

        return instance;
    }

    private boolean next() throws IOException {
        if (drained) {
            int i = reader.read();
            if (i == -1) {
                return false;
            }

            drained = false;
            c = (char) i;
        }
        return true;
    }

    private char consume() throws IOException {
        drained = true;
        return c;
    }

    private char peek() throws IOException {
        if (drained) {
            next();
        }
        return c;
    }

    private void skipSpace() throws IOException {
        while (next()) {
            if (!Character.isWhitespace(consume())) {
                drained = false;
                return;
            }
        }
    }

    private void skipComments() throws IOException {
        skipSpace();
        while (peek() == COMMENT) {
            while (next()) {
                char c = consume();
                if (isLineBreak(c)) {
                    skipComments();
                    return;
                }
            }
        }
    }

    private String nextString(boolean key) throws IOException {
        int pos = 0;
        while (next()) {
            char c = consume();
            if ((key && c == ASSIGN) || isLineBreak(c)) {
                break;
            }
            buffer[pos++] = c;
        }
        return new String(buffer, 0, pos);
    }

    private boolean isLineBreak(char c) {
        return c == NEWLINE || c == LINE_SEPARATOR;
    }

    @Override
    public void close() throws IOException {
        source.close();
        reader.close();
    }
}
