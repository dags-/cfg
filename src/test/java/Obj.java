import me.dags.config.style.Default;
import me.dags.config.style.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
@Style(ignoreEmpty = true, breaks = 0)
public class Obj {

    @Default("")
    public String name = "";

    @Default("0")
    public int age = 0;

    public List<String> list = new ArrayList<>();

    @Override
    public String toString() {
        return "Obj{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", list=" + list +
                '}';
    }

    public static Obj getDef() {
        Obj obj = new Obj();
        obj.name = "Harry";
        obj.age = 34;
        obj.list = Arrays.asList("one", "two", "three");
        return obj;
    }
}
