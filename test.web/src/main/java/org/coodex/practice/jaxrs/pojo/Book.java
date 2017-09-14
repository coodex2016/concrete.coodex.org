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

package org.coodex.practice.jaxrs.pojo;

import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.mockers.ID;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by davidoff shen on 2016-11-28.
 */
public class Book extends BookInfo {

    private final static AtomicLong ID_SEQUENCE = new AtomicLong(1);


    private final long id = ID_SEQUENCE.getAndIncrement();


    public Book() {
        super(null, null, 0);
    }

    public Book(String bookName, String author, int price) {
        super(bookName, author, price);
    }

    @Description(name = "书本ID", description = "主键，唯一标识")
    @ID
    public long getId() {
        return id;
    }


}
