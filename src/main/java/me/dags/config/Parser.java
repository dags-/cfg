package me.dags.config;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
class Parser implements Closeable {

    private final Reader reader;
    private final char[] buffer = new char[4096];

    private char c = (char) -1;
    private boolean drained = true;

    Parser(Reader reader) {
        this.reader = reader;
    }

    @SuppressWarnings("unchecked")
    <T> T unMarshal(T owner, Node element) throws Exception {
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
            skipSpace(false);
            String input = nextString(key);
            ValueNode node = element.asValue();
            return node.parse(input);
        }
    }

    private Object populateObject(Object objectOwner, ObjectNode object) throws Exception {
        // skip header comments, pad & object start char
        skipComments();
        if (peek() == Render.START_OBJECT) {
            consume();
        }

        while (next()) {
            skipSpace(true);

            // check if end of object
            if (peek() == Render.END_OBJECT) {
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
        // skip pad & map start char
        skipSpace(false);
        if (peek() == Render.START_OBJECT) {
            consume();
        }

        Map instance = map.getMap(mapOwner);
        instance.clear();

        Node keyElement = map.getKeyTemplate();
        Node valueElement = map.getValueTemplate();

        while (next()) {
            skipSpace(true);

            // check if end of map
            if (peek() == Render.END_OBJECT) {
                consume();
                break;
            }

            // parse key, skip assign char ':'
            Object key = parse(map.newKeyInstance(), keyElement, true);
            if (peek() == Render.ASSIGN) {
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
        // skip pad and start list char
        skipSpace(false);
        if (peek() == Render.START_LIST) {
            consume();
        }

        List instance = list.getList(owner);
        instance.clear();

        Node node = list.getValueTemplate();
        while (next()) {
            skipSpace(true);

            // check if end of array
            if (peek() == Render.END_LIST) {
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

    private void skipSpace(boolean lineBreaks) throws IOException {
        while (next()) {
            char c = consume();
            if (!lineBreaks && isLineBreak(c)) {
                drained = false;
                return;
            }
            if (!Character.isWhitespace(c)) {
                drained = false;
                return;
            }
        }
    }

    private void skipComments() throws IOException {
        skipSpace(true);
        while (peek() == Render.COMMENT) {
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
        char peek = peek();
        if (peek == Render.ESCAPE || peek == Render.QUOTE) {
            consume();
            return nextQuotedString(peek);
        }
        return nextRawString(key);
    }

    private String nextRawString(boolean key) throws IOException {
        int pos = 0;
        while (next()) {
            char c = consume();
            if ((key && c == Render.ASSIGN) || isLineBreak(c)) {
                break;
            }
            buffer[pos++] = c;
        }
        return new String(buffer, 0, pos);
    }

    private String nextQuotedString(char end) throws IOException {
        int pos = 0;
        while (next()) {
            if (consume() == end) {
                break;
            }
            buffer[pos++] = c;
        }
        return new String(buffer, 0, pos);
    }

    private boolean isLineBreak(char c) {
        return c == Render.NEWLINE || c == Render.LINE_SEPARATOR;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
