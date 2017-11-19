package me.dags.config;

import me.dags.config.style.Comment;
import me.dags.config.style.Style;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
final class Render {

    static final char LINE_SEPARATOR = System.lineSeparator().charAt(0);
    static final char START_OBJECT = '{';
    static final char END_OBJECT = '}';
    static final char START_LIST = '[';
    static final char END_LIST = ']';
    static final char NEWLINE = '\n';
    static final char COMMENT = '#';
    static final char ASSIGN = ':';
    static final char ESCAPE = '`';
    static final char QUOTE = '\'';

    static final char PAD = ' ';

    private Render() {}

    static void lineEnd(Appendable appendable) throws IOException {
        appendable.append(NEWLINE);
    }

    static void startObject(Appendable appendable) throws IOException {
        appendable.append(START_OBJECT);
    }

    static void endObject(Appendable appendable) throws IOException {
        appendable.append(END_OBJECT);
    }

    static void startList(Appendable appendable) throws IOException {
        appendable.append(START_LIST);
    }

    static void endList(Appendable appendable) throws IOException {
        appendable.append(END_LIST);
    }

    static void assign(Appendable appendable, Style style) throws IOException {
        appendable.append(ASSIGN);
        pad(appendable, style);
    }

    static void key(Appendable appendable, Object key, Style style) throws IOException {
        appendable.append(key.toString());
        assign(appendable, style);
    }

    static void indents(Appendable appendable, Style style, int level) throws IOException {
        while (level-- > 0) {
            repeat(appendable, style.indent(), PAD);
        }
    }

    static void pad(Appendable appendable, Style style) throws IOException {
        repeat(appendable, style.pad(), PAD);
    }

    static void lineBreaks(Appendable appendable, Style style) throws IOException {
        repeat(appendable, style.breaks(), NEWLINE);
    }

    static boolean header(Appendable appendable, Comment header, Style style, int level, boolean root) throws IOException {
        if (style.comments() && header != null && header.value().length > 0) {
            if (root) {
                return comment(appendable, header, style, level);
            } else {
                appendable.append(Render.NEWLINE);
                return comment(appendable, header, style, level);
            }
        }
        return false;
    }

    static boolean comment(Appendable appendable, Comment comment, Style style, int level) throws IOException {
        if (comment != null && style.comments()) {
            for (String line : comment.value()) {
                Render.indents(appendable, style, level);
                appendable.append(Render.COMMENT);
                appendable.append(Render.PAD);
                appendable.append(line);
                appendable.append(Render.NEWLINE);
            }
            return comment.value().length > 0;
        }
        return false;
    }

    private static void repeat(Appendable appendable, int count, char c) throws IOException {
        while (count-- > 0) {
            appendable.append(c);
        }
    }
}
