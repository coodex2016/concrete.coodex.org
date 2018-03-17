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
