import me.dags.config.Mapper;

import java.io.File;

/**
 * @author dags <dags@dags.me>
 */
public class Test {

    public static void main(String[] args) throws Exception {
        Mapper<Obj> mapper = Mapper.of(Obj.class);

        // reads test0.conf or creates & writes a new Obj
        Obj obj = mapper.must(new File("test0.conf"), Obj::getDef);
        System.out.println(obj);

        obj.name = ""; // Field is omitted on next write due to @Default("")
        mapper.write(obj, new File("test1.conf"));
    }
}
