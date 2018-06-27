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

package test.org.coodex.concrete.couries.activemq;

import org.coodex.concrete.message.GenericTypeHelper;
import org.coodex.concrete.message.Observer;
import org.coodex.concrete.message.Topic;
import org.coodex.concrete.message.Topics;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicTest {

    private final static Logger log = LoggerFactory.getLogger(TopicTest.class);


    private static Topic<String> topic1 = Topics.<String, Topic<String>>get(
            new GenericTypeHelper.GenericType<Topic<String>>() {

            }.getType()
            , "topic1");
    private static Topic2 topic2 = Topics.<String, Topic2>get(Topic2.class, "topic1");

    @BeforeClass
    public static void subscribe() {
        topic1.subscribe(new Observer<String>() {
            @Override
            public void update(String message) throws Throwable {
                log.debug("topic1 received: {}", message);
            }
        });

        topic2.subscribe(new Observer<String>() {
            @Override
            public void update(String message) throws Throwable {
                log.debug("topic2 received: {}", message);
            }
        });
    }

    @Test
    public void send() {
        topic1.publish("hello from topic1");
        topic2.publish("hello from topic2");

        synchronized (this) {
            try {
                wait(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface Topic2 extends Topic<String> {
    }
}
