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

package org.coodex.practice.jaxrs.api;

import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.api.ServiceTiming;
import org.coodex.practice.jaxrs.pojo.*;

import java.util.List;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-11-28.
 */
@MicroService("book")

public interface ServiceExample extends ServiceB, GenericService<D, D> {
    @ServiceTiming("rule2")
    String tokenId();

    List<String> genericTest(List<Integer> x);

    Map<String, BookInfo> genericTest2(Map<String, Book> y);

    List<List<BookInfo>> genericTest3(List<List<BookInfo>> z);

    GenericPojo<Book> genericTest4(GenericPojo<BookInfo> gp);

    GenericPojo<Book> genericTest5(List<GenericPojo<BookInfo>> gp);

    G2<GenericPojo<String>, GenericPojo<Integer>> g5(G2<GenericPojo<String>, GenericPojo<Integer>> xx);

    GenericPojo<GenericPojo<Book>> g6(GenericPojo<GenericPojo<Book>> gp);

    void multiPojo(String pathParam, List<int[]> body1, GenericPojo<BookInfo> body2, Book body3, int[] body4);

}
