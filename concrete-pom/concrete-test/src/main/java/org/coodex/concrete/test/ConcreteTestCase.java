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

import org.coodex.util.Clock;
import org.junit.Rule;
import org.junit.rules.TestRule;

/**
 * Created by davidoff shen on 2016-09-08.
 */
public abstract class ConcreteTestCase {
    @Rule
    public final TestRule CONCRETE_TEST_RULE = new ConcreteTestRule();

    protected void sleep(long millis) {
        try {
            Clock.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
