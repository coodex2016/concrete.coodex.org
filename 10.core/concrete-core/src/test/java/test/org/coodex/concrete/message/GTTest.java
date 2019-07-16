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

package test.org.coodex.concrete.message;

import org.coodex.util.GenericTypeHelper;
import org.coodex.concrete.message.TokenBasedTopic;
import org.coodex.concrete.message.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class GTTest {

    private final static Logger log = LoggerFactory.getLogger(GTTest.class);

    static class A<X extends Serializable> {
        void testA() {
            log.debug("{}",
                    new GenericTypeHelper.GenericType<Topic<X>>(getClass()) {
                    }.getType());
        }
    }

    static class B<X extends Serializable> extends A<X> {
        void testB() {
            log.debug("{}", new GenericTypeHelper.GenericType<TokenBasedTopic<X>>(this.getClass()) {
            }.getType());
        }
    }

    static class C<X extends Serializable> extends B<X> {
        void testC() {
        }
    }

    static class D extends C<String>{}

    public static void main(String [] args){
        D c = new D();
        c.testA();
        c.testB();
        c.testC();
    }
}
