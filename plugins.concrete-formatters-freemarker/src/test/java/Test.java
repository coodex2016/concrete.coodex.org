import org.coodex.concrete.common.MessageFormatter;
import org.coodex.concrete.formatters.FreemarkerMessageFormatter;

/**
 * Created by davidoff shen on 2016-12-02.
 */
public class Test {

    public static void main(String [] args){
        MessageFormatter formatter = new FreemarkerMessageFormatter();
        System.out.println(
                formatter.format("${o1}, ${o2}, ${o3.class}", "hello", 2, formatter));
    }
}
