package me.dags.config.style;

import java.lang.annotation.*;

/**
 * @author dags <dags@dags.me>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Style {

    /**
     * Number of line-breaks between entries
     */
    int breaks() default 1;

    /**
     * Number of spaces between key/value pairs
     */
    int pad() default 1;

    /**
     * Number of spaces in an indent level
     */
    int indent() default 1;

    /**
     * Render comments
     */
    boolean comments() default true;

    /**
     * Override parent class' style
     */
    boolean override() default false;

    /**
     * Do not draw empty maps/lists
     */
    boolean ignoreEmpty() default false;

    /**
     * Default draw style
     */
    Style DEFAULT = new Style() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Style.class;
        }

        @Override
        public int breaks() {
            return 1;
        }

        @Override
        public int pad() {
            return 1;
        }

        @Override
        public int indent() {
            return 1;
        }

        @Override
        public boolean comments() {
            return true;
        }

        @Override
        public boolean override() {
            return false;
        }

        @Override
        public boolean ignoreEmpty() {
            return false;
        }
    };
}
