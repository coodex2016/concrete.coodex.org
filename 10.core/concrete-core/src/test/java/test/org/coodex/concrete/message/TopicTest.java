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
import org.coodex.concrete.message.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class TopicTest {
    private final static Logger log = LoggerFactory.getLogger(TopicTest.class);

    public static void main(String[] args) {
//        B a = new B();
//        Topic<String> topic = Topics.get(new GenericType<Topic<String>>() {
//        });
//        topic.subscribe(new Observer<String>() {
//            @Override
//            public void update(String o) throws Throwable {
//                log.debug(o);
//            }
//        });
//
//        topic.publish("hello topic.");
        A<String[]> a = new A<String[]>() {
        };
        B b = new B();
        a.getTopic();
        b.getTopic();
//        System.out.println(new TypeReference<A<String>>(){}.getType());

    }

    static class A<M extends Serializable> {
        Topic<M> getTopic() {
            System.out.println(new GenericTypeHelper.GenericType<M>(getClass()) {
            }.getType());

            return null;
//            return Topics.get(new GenericType<Topic<M>>() {
//            });
        }
    }

    static class B extends A<String> {

    }
}
