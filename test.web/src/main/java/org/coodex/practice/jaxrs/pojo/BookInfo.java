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

import org.coodex.pojomocker.annotations.INTEGER;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class BookInfo {
    private String bookName;
    private String author;
    @INTEGER(min = 100)
    private int price; //分

    public BookInfo(){

    }

    public BookInfo(String bookName, String author, int price) {
        this.bookName = bookName;
        this.author = author;
        this.price = price;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAuthor() {
        return author;
    }

    public int getPrice() {
        return price;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "BookInfo{" +
                "bookName='" + getBookName() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", price=" + getPrice() +
                '}';
    }
}
