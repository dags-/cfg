import me.dags.config.MapperFactory;

import java.io.File;

/**
 * @author dags <dags@dags.me>
 */
public class Test {

    public static void main(String[] args) throws Exception {
        test(Double.class, 0.3475, "test0");
        test(Info.class, new Info(), "test1");
        test(Wrapper.class, new Wrapper(), "test2");
    }

    private static <T> void test(Class<T> type, T val, String testName) throws Exception {
        File file = new File(String.format("%s.cfg", testName));
        MapperFactory<T> factory = MapperFactory.of(type);
        factory.write(val, file);
        System.out.println(factory.read(file));
    }
}
