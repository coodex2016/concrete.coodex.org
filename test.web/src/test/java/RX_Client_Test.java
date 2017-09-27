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

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.coodex.concrete.rx.RXClient;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.practice.jaxrs.pojo.Book;
import rx.org.coodex.practice.jaxrs.api.ServiceExample_RX;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class RX_Client_Test {

    private static ScheduledExecutorService executorService = ExecutorsHelper.newScheduledThreadPool(1);

    public static void main(String [] args) {

//        Runnable runnable = new Runnable() {
//            int count = 0;
//
//            @Override
//            public void run() {
//                try {
//                    /// todo
//                    System.out.println(count++);
//                } finally {
//                    executorService.schedule(this, 10, TimeUnit.MILLISECONDS);
//                }
//            }
//        };
//
//
//        executorService.execute(runnable);

//        System.out.println(("ddd", String.class));
//        System.out.println(JSONSerializerFactory.getInstance().parse("ddd", int.class));
//        if(true) return;
        String [] domains = {"http://localhost:8080/jaxrs", "ws://localhost:8080/WebSocket"};
//        ServiceExample_RX serviceExample_rx = RXClient.getInstance(ServiceExample_RX.class, "ws://localhost:8080/WebSocket");
//        serviceExample_rx.add(1,2).subscribe(new Observer<Integer>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(Integer integer) {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
//        SaaSExample_RX saaSExample_rx = RXClient.getInstance(SaaSExample_RX.class, domains[1]);
//        saaSExample_rx.exampleForSaaS("w123","ddd").subscribe(new Observer<String>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//
//            }
//
//            @Override
//            public void onNext(String s) {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onComplete() {
//
//            }
//        });
        for(final String domain: domains) {
            ServiceExample_RX rx = RXClient.getInstance(ServiceExample_RX.class, domain);
            rx.tokenId().subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onNext(String tokenId) {
                    synchronized (RX_Client_Test.class) {
                        System.out.println(tokenId);
//                        System.out.println(domain);
//                        for (Book book : books) {
//                            System.out.println(book);
//                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onComplete() {
                    System.out.println("complete");
                }
            });
        }
    }
}
