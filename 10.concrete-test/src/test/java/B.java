import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Created by davidoff shen on 2016-09-08.
 */
public class B implements TestRule{

    @Override
    public Statement apply(Statement base, Description description) {
        System.out.println(description);

        return base;
    }
}
