/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.coodex.practice.jaxrs.api;

import org.coodex.concrete.api.*;
import org.coodex.concrete.jaxrs.BigString;
import org.coodex.practice.jaxrs.pojo.Book;
import org.coodex.practice.jaxrs.pojo.BookInfo;

/**
 * Created by davidoff shen on 2016-11-28.
 */
@MicroService("A")
@Abstract
public interface ServiceA extends ConcreteService {

//    @MicroService("getBean")
//    String helloWorld(@PathParam("userName") String userName);

    @ServiceTiming("rule1")
    Book get(long bookId);

    String bigStringTest(String pathParam, @BigString String toPost);

    Book get(String author, long price);

    String update(long bookId, BookInfo book);

    String delete(long bookId);

    @AccessAllow()
    String checkRole();


}
