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
import org.coodex.practice.jaxrs.pojo.Book;
import rx.org.coodex.practice.jaxrs.api.ServiceExample_RX;

import java.util.List;

public class RX_Client_Test {

    public static void main(String [] args){
        String [] domains = {"http://localhost:8080", "ws://localhost:8080/WebSocket"};
        for(final String domain: domains) {
            ServiceExample_RX rx = RXClient.getInstance(ServiceExample_RX.class, domain);
            rx.findByPriceLessThen(6000).subscribe(new Observer<List<Book>>() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onNext(List<Book> books) {
                    synchronized (RX_Client_Test.class) {
                        System.out.println(domain);
                        for (Book book : books) {
                            System.out.println(book);
                        }
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
