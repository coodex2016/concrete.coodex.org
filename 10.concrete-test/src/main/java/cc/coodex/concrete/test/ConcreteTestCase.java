package cc.coodex.concrete.test;

import org.junit.Rule;
import org.junit.rules.TestRule;

/**
 * Created by davidoff shen on 2016-09-08.
 */
public abstract class ConcreteTestCase {
    @Rule
    public final TestRule CONCRETE_TEST_RULE = new ConcreteTestRule();
}
