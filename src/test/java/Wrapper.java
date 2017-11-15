import me.dags.config.annotation.Comment;

/**
 * @author dags <dags@dags.me>
 */
@Comment("Wraps an Info thing")
public class Wrapper {

    private Info info = new Info();

    @Override
    public String toString() {
        return "Wrapper{" +
                "info=" + info +
                '}';
    }
}
