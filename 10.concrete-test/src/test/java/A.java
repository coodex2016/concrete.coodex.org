import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.concrete.test.ConcreteTestCase;
import org.coodex.concrete.test.TokenID;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Created by davidoff shen on 2016-09-08.
 */
public class A extends ConcreteTestCase {


    @BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    private Token token = TokenWrapper.getInstance();

    @Before
    public void before() {
        System.out.println("before");
    }

    @Test
    @TokenID("1")
    public void test() {
        token.setAttribute("test", "test");
    }

    @Test
    @TokenID("1")
    public void test2() {
        System.out.println(token.getAttribute("test"));
    }

    @Test
    public void test3() {
        System.out.println(token.getAttribute("test"));
    }
}
