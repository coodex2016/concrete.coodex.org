package org.coodex.concrete.test;

import org.coodex.concrete.common.ConcreteClosure;
import org.coodex.concrete.core.token.TokenWrapper;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Created by davidoff shen on 2016-09-08.
 */
public class ConcreteTestRule implements TestRule {


    @Override
    public Statement apply(final Statement base, final Description description) {

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                TokenWrapper.closure(ConcreteTokenProvider.getToken(description), new ConcreteClosure() {
                    @Override
                    public Object concreteRun() throws Throwable {
                        base.evaluate();
                        return null;
                    }
                });
            }
        };
    }
}
