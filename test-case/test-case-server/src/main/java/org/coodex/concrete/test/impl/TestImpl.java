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
import org.coodex.concrete.test.api.Test;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.org.coodex.concrete.test.api.Test_RX;

import javax.inject.Inject;

public class TestImpl implements Test {

    private final static Logger log = LoggerFactory.getLogger(TestImpl.class);


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


    @Override
    public int add(int x1, int x2) {
        return x1 + x2;
    }

    @Override
    public String sayHello(String name) {
        invokeSync(local, "local");
        invokeSync(jaxrs, "jaxrs");
        invokeSync(websocket, "websocket");
        invokeRx(localRx, "local");
        invokeRx(jaxrsRx, "jaxrs");
        invokeRx(websocketRx, "websocket");
        return "Hello " + name;
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
