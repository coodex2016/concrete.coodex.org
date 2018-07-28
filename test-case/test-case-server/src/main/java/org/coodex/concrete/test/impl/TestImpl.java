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

package org.coodex.concrete.test.impl;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.coodex.concrete.ConcreteClient;
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.concrete.message.MessageFilter;
import org.coodex.concrete.message.Queue;
import org.coodex.concrete.message.TokenBasedTopic;
import org.coodex.concrete.message.Topic;
import org.coodex.concrete.test.api.Test;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.org.coodex.concrete.test.api.Test_RX;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

public class TestImpl implements Test {

    private final static Logger log = LoggerFactory.getLogger(TestImpl.class);

    private Token token = TokenWrapper.getInstance();

    @Queue("test")
    private Topic<String> x;

//    @Queue("test")
//    private Topic<Set<String>> y;

    @Queue("test")
    private TokenBasedTopic<TestSubject> tokenBasedTopic;

//    @Inject
//    @Queue("test")
//    private Topic<String> z;

    @Inject
    @ConcreteClient("local")
    private Test local;

    @Inject
    @ConcreteClient("local")
    private Test_RX localRx;

    @Inject
    @ConcreteClient("jaxrs")
    private Test jaxrs;

    @Inject
    @ConcreteClient("jaxrs")
    private Test_RX jaxrsRx;

    @Inject
    @ConcreteClient("websocket")
    private Test websocket;

    @Inject
    @ConcreteClient("websocket")
    private Test_RX websocketRx;


    private boolean pushStart = false;


    private void startPush(){
        if(!pushStart){
            synchronized (this){
                if(!pushStart){
                    pushStart = true;
                    for(int i = 0; i < 10; i ++){
                        final int finalI = i;
                        new Thread(){
                            @Override
                            public void run() {
                                while(true) {
                                    tokenBasedTopic.publish(new TestSubject(finalI));
                                    try {
                                        Thread.sleep(Common.random(2000, 4000));
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }.start();
                    }
                }
            }
        }
    }

    @Override
    public int add(final int x1, int x2) {
        token.setAttribute("a", "sdf");
        log.debug("tokenId: {}", token.getTokenId());
        tokenBasedTopic.subscribe(new MessageFilter<TestSubject>() {
            @Override
            public boolean handle(TestSubject message) {
                return message.getNumber() == x1;
            }
        });

        startPush();
        return x1 + x2;
    }

    @Inject
    private Subjoin subjoin;

    @Override
    public String sayHello(String name) {
        for(String key: subjoin.keySet()){
            log.debug("{}: {}", key.toUpperCase(), subjoin.get(key.toUpperCase()));
        }

        try {
            Thread.sleep(Common.random(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        x.publish("hello");
//        invokeSync(local, "local");
//        invokeSync(jaxrs, "jaxrs");
//        invokeSync(websocket, "websocket");
//        invokeRx(localRx, "local");
//        invokeRx(jaxrsRx, "jaxrs");
//        invokeRx(websocketRx, "websocket");
        return "Hello " + name;
    }

    @Override
    public void bodyTest(Integer bodyInt, Integer notBodyInt, String bodyStr, String notBodyStr) {
        log.debug("{}, {}, {}, {}", bodyInt, notBodyInt, bodyStr, notBodyStr);
    }

    private static ExecutorService executorService = ExecutorsHelper.newFixedThreadPool(4);
    @Override
    public Float test() {


        APM.parallel(executorService, new Runnable() {
            @Override
            public void run() {
                jaxrs.sayHello("1100");
            }
        }, new Runnable() {
            @Override
            public void run() {
                jaxrs.sayHello("1200");
            }
        }, new Runnable() {
            @Override
            public void run() {
                jaxrs.sayHello("1300");
            }
        }, new Runnable() {
            @Override
            public void run() {
                jaxrs.sayHello("1400");
            }
        }, new Runnable() {
            @Override
            public void run() {
                jaxrs.sayHello("1500");
            }
        });



        return 0.45f;
    }

    private void invokeRx(final Test_RX test, final String tag) {
        final int x1 = Common.random(100);
        final int x2 = Common.random(100);
        test.add(x1, x2).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                log.info("invoke add ({}, {}) = {} from [{}]", x1, x2, integer, tag);
            }

            @Override
            public void onError(Throwable e) {
                log.info("invoke add ({}, {}) = ? from [{}]", x1, x2, tag, e);
            }

            @Override
            public void onComplete() {
                log.info("invoke add ({}, {}) complete", x1, x2);
            }
        });
    }

    private void invokeSync(Test test, String tag) {
        int x1 = Common.random(100);
        int x2 = Common.random(100);
        log.info("invoke add ({}, {}) = {} from [{}]", x1, x2, test.add(x1, x2), tag);
    }
}
