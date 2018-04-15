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

import org.coodex.concrete.Client;
import org.coodex.concrete.test.api.Test;

public class ClientInvoker {

    public static void main(String [] args){
//        Test test = Client.getInstance(Test.class,"websocket");
        Test test = Client.getInstance(Test.class,"remote");
        System.out.println(String.format("1 + 2 = %d", test.add(1,2)));
        System.out.println(test.sayHello("Davidoff"));
//        System.out.println(test.add(1,2));

//        Test_RX test = Client.getInstance(Test_RX.class,"websocket");
//        test.add(1,2).subscribe(new Observer<Integer>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                System.out.println(d);
//            }
//
//            @Override
//            public void onNext(Integer integer) {
//                System.out.println(integer);
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
    }
}
