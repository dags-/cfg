import me.dags.config.Mapper;

import java.io.File;
import java.util.HashMap;

/**
 * @author dags <dags@dags.me>
 */
public class Test {

    public static void main(String[] args) throws Exception {
        Mapper<Child> mapper = Mapper.of(Child.class);

        Child child = mapper.must(new File("child.conf"), () -> {
            Child c = new Child();
            c.put("one", 2);
            c.put("adsa", 3);
            c.put("gaga", 5);
            return c;
        });

        System.out.println("IN: " + child);
    }

    public static class GrandParent<Q> extends HashMap<Q, Integer> {

    }

    public static class Parent<Q, K, V> extends GrandParent<K> {

    }

    public static class Child extends Parent<Integer, String, Float> {

        private final Child $self = this;
    }
}
