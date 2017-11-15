import me.dags.config.annotation.Comment;
import me.dags.config.annotation.Order;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author dags <dags@dags.me>
 */
@Comment({"Stores some things about stuff", "Set the stuff to the things"})
@Order({"variants", "level", "name"})
public class Info {

    private String name = "derrick";
    private int level = 25;
    private float opacity = 0.5F;
    private Map<Integer, String> variants = variants("some_thing", "and_another");

    @Override
    public String toString() {
        return "Config{" +
                "name='" + name + '\'' +
                ", level=" + level +
                ", opacity=" + opacity +
                ", variants=" + variants +
                '}';
    }

    private static Map<Integer, String> variants(String... vals) {
        Map<Integer, String> variants = new LinkedHashMap<>();
        Random random = new Random();
        for (int i = 0 ; i < vals.length; i++) {
            variants.put(random.nextInt(100), vals[i]);
        }
        return variants;
    }
}
