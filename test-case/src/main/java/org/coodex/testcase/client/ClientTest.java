/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.testcase.client;

import org.coodex.concrete.Client;
import org.coodex.testcase.api.TestCase;

public class ClientTest {

    private static void test(String module){
        TestCase testCase = Client.getInstance(TestCase.class,module);
        System.out.println(testCase.add(1,2) == 3);
    }

    public static void main(String[] args) {
//        test("jaxrs");
        test("amqp");

        test("amqp");

        test("jaxrs");

        test("websocket");
    }
}
