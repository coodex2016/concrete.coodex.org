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

package org.coodex.practice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

public class JaxrsClient {
    private final static Logger log = LoggerFactory.getLogger(JaxrsClient.class);

    public static void main(String[] args) throws InterruptedException {
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(()-> {
//            System.out.println("run");
//            return 1;
//        }, ExecutorsHelper.newFixedThreadPool(1,"test"));
//        Thread.sleep(1000);
//        future.whenCompleteAsync(new BiConsumer<Integer, Throwable>() {
//            @Override
//            public void accept(Integer integer, Throwable throwable) {
//                System.out.println(integer);
//            }
//        });
////                .exceptionally(new Function<Throwable, Integer>() {
////            @Override
////            public Integer apply(Throwable throwable) {
////                return null;
////            }
////        });//.thenApply(Integer::intValue);
        CompletionStage<Response> completionStageRxInvoker = ClientBuilder.newClient()
                .target("https://coodex.org").path("19880904").request()
                .rx()
                .get();
        try {
            Response o = completionStageRxInvoker
                    .handleAsync((BiFunction<Response, Throwable, Response>) (response, throwable) -> {

                        throw new RuntimeException("sadfasdf");
                    })

                    .handleAsync(
                            (BiFunction<Response, Throwable, Response>) (response, throwable) -> {
                                log.info("throwable: {}", throwable.getLocalizedMessage());
                                return response;
                            }
                    )

                    .whenCompleteAsync((response, throwable) -> {
                        log.info("response: {}\nthrowable: {}", response, throwable);
//                        throw new RuntimeException();
                    }).exceptionally(throwable -> {
                        log.info("throwable: {}", throwable.getLocalizedMessage(), throwable);
//                        throw new RuntimeException(throwable);
                        return null;
                    }).toCompletableFuture().get();

            log.info("result: {}", o);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
}
