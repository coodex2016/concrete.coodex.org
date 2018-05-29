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

package org.coodex.concrete.test.client;


import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.message.*;
import org.coodex.concrete.test.ConcreteTestCase;
import org.coodex.concrete.test.TokenID;
import org.coodex.util.Common;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TBT_Test extends ConcreteTestCase {

    private final static Logger log = LoggerFactory.getLogger(TBT_Test.class);


    private static TokenBasedTopic<TestSubject> topic = Topics.<TestSubject, TokenBasedTopic<TestSubject>>get(
            new GenericTypeHelper.GenericType<TokenBasedTopic<TestSubject>>() {
            }.getType()
    );


    @TokenID("123")
    @Test
    public void test1() {
        topic.subscribe(new MessageFilter<TestSubject>() {
            @Override
            public boolean handle(TestSubject message) {
                return message.getNumber() % 2 == 0;
            }
        });
    }

    @TokenID("345")
    @Test
    public void test2() {
        topic.subscribe(new MessageFilter<TestSubject>() {
            @Override
            public boolean handle(TestSubject message) {
                return message.getNumber() % 2 == 0;
            }
        });
    }


    @TokenID("123")
    @Test
    public void test3() {
        topic.subscribe(new MessageFilter<TestSubject>() {
            @Override
            public boolean handle(TestSubject message) {
                return message.getNumber() % 2 == 1;
            }
        });
    }

    private void polling(final String tokenId) {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    List<ServerSideMessage> list = TBMContainer.getInstance().getMessages(tokenId, 15000);
                    log.debug("token [{}] received {} messages.\n{}", tokenId, list.size(), JSONSerializerFactory.getInstance().toJson(list));
                }
            }
        }.start();
    }

    @Test
    public void startPolling() {
        polling("123");
        polling("345");
    }


    private void publish(){
        while(true){
            topic.publish(new TestSubject());
            try {
                Thread.sleep(Common.random(500, 4000));
            } catch (InterruptedException e) {
            }
        }
    }


    @Test
    public void xtartPublish(){
        for(int i = 0; i < 10; i ++){
            new Thread(){
                @Override
                public void run() {
                    publish();
                }
            }.start();
        }
        publish();
    }


    public static class TestSubject implements Subject {

        private int number = Common.random(100);

        @Override
        public String getSubject() {
            return "test";
        }

        public int getNumber() {
            return number;
        }
    }
}
