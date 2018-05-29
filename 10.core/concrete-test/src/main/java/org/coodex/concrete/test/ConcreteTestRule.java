/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

import org.coodex.closure.CallableClosure;
import org.coodex.concrete.common.AbstractSubjoin;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.Subjoin;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Arrays;
import java.util.Collection;

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
                TokenID testToken = description.getAnnotation(TokenID.class);
                String tokenId = testToken == null ? null : testToken.value();
                try {
                    runWithContext(
                            new TestServiceContext(tokenId, getSubjoin(description)),
                            new CallableClosure() {
                                @Override
                                public Object call() throws Throwable {
                                    base.evaluate();
                                    return null;
                                }
                            });
                } catch (ConcreteException ce) {
                    throw (ce.getCause() != null && ce.getCode() == ErrorCodes.UNKNOWN_ERROR) ? ce.getCause() : ce;
                }
            }
        };
    }

    private Subjoin getSubjoin(Description description) {
        Subjoin subjoin = new AbstractSubjoin() {
            @Override
            protected Collection<String> skipKeys() {
                return null;
            }
        };
        TestSubjoin testSubjoin = description.getAnnotation(TestSubjoin.class);
        if (testSubjoin != null && testSubjoin.value().length > 0) {
            for (TestSubjoinItem testSubjoinItem : testSubjoin.value()) {
                subjoin.set(testSubjoinItem.key(), Arrays.asList(testSubjoinItem.value()));
            }
        }
        return subjoin;
    }


//    private Subjoin getSubjoin() {
//        return new SubjoinWrapper.DefaultSubjoin();
//    }


}
