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

import org.coodex.concrete.api.Abstract;
import org.coodex.concrete.api.AccessAllow;
import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.api.ServiceTiming;
import org.coodex.concrete.api.mockers.IdCard;
import org.coodex.concrete.jaxrs.BigString;
import org.coodex.practice.jaxrs.pojo.Book;
import org.coodex.practice.jaxrs.pojo.BookInfo;
import org.coodex.util.Parameter;

/**
 * Created by davidoff shen on 2016-11-28.
 */
@MicroService("A")
@Abstract
public interface ServiceA extends Calc {

//    @MicroService("getBean")
//    String helloWorld(@PathParam("userName") String userName);

    @ServiceTiming("rule1")
    Book get(@Parameter("bookId") long bookId);

    @IdCard
    String bigStringTest(String pathParam, @BigString String toPost);

    Book get(@Parameter("author") String author,
             @Parameter("price") long price);

    @IdCard
    String update(
            @Parameter("bookId") long bookId,
            @Parameter("book") BookInfo book);

    @IdCard
    String delete(@Parameter("bookId") long bookId);

    @AccessAllow()
    String checkRole();


}
