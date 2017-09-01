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

package rx.org.coodex.practice.jaxrs.api;

import org.coodex.concrete.rx.ReactiveExtensionFor;

import io.reactivex.Observable;

import java.util.List;
import java.util.Map;
import org.coodex.practice.jaxrs.api.ServiceExample;
import org.coodex.practice.jaxrs.pojo.Book;
import org.coodex.practice.jaxrs.pojo.BookInfo;
import org.coodex.practice.jaxrs.pojo.D;
import org.coodex.practice.jaxrs.pojo.G2;
import org.coodex.practice.jaxrs.pojo.GenericPojo;

/**
 * Create by concrete-api-tools.
 */
@ReactiveExtensionFor(ServiceExample.class)
public interface ServiceExample_RX {

    Observable<Book> get(long bookId);

    Observable<Map<String, BookInfo>> genericTest2(Map<String, Book> y);

    Observable<String> delete(long bookId);

    Observable<List<Book>> all();

    Observable<String> update(long bookId, BookInfo book);

    Observable<D> genericTest1001(D x);

    Observable<Void> multiPojo(String pathParam, List<int[]> body1, GenericPojo<BookInfo> body2, Book body3, int[] body4);

    Observable<Book> get(String author, long price);

    Observable<String> bigStringTest(String pathParam, String toPost);

    Observable<GenericPojo<Book>> genericTest4(GenericPojo<BookInfo> gp);

    Observable<GenericPojo<Book>> genericTest5(List<GenericPojo<BookInfo>> gp);

    Observable<List<String>> genericTest(List<Integer> x);

    Observable<List<Book>> findByPriceLessThen(int price);

    Observable<Integer> add(int x, int y);

    Observable<G2<GenericPojo<String>, GenericPojo<Integer>>> g5(G2<GenericPojo<String>, GenericPojo<Integer>> xx);

    Observable<String> tokenId();

    Observable<String> checkRole();

    Observable<List<List<BookInfo>>> genericTest3(List<List<BookInfo>> z);

    Observable<List<D>> genericTest1002(List<D> x);

    Observable<List<Book>> findByAuthorLike(String author);

    Observable<GenericPojo<GenericPojo<Book>>> g6(GenericPojo<GenericPojo<Book>> gp);


}