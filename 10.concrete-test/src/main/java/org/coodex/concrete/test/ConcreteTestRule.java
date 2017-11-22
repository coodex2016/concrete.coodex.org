/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.test;

import org.coodex.concrete.common.*;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.coodex.concrete.common.ConcreteContext.runWithContext;

/**
 * Created by davidoff shen on 2016-09-08.
 */
public class ConcreteTestRule implements TestRule {

    @Override
    public Statement apply(final Statement base, final Description description) {

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    runWithContext(
                            new TestServiceContext(
                                    ConcreteTokenProvider.getToken(description),
                                    getSubjoin()),
                            new ConcreteClosure() {
                                @Override
                                public Object concreteRun() throws Throwable {
                                    base.evaluate();
                                    return null;
                                }
                            });
//                    runWith("TEST", getSubjoin(),
//                            ConcreteTokenProvider.getToken(description),
//                            ConcreteContext.run(SIDE, SIDE_TEST, new ConcreteClosure() {
//                                @Override
//                                public Object concreteRun() throws Throwable {
//                                    base.evaluate();
//                                    return null;
//                                }
//                            }));
                } catch (ConcreteException ce) {
                    throw (ce.getCause() != null && ce.getCode() == ErrorCodes.UNKNOWN_ERROR) ? ce.getCause() : ce;
                }
            }
        };
    }

    private Subjoin getSubjoin() {
        return new SubjoinWrapper.DefaultSubjoin();
    }


}
