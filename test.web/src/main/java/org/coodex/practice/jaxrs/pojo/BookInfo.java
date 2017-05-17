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

import org.coodex.concrete.api.mockers.*;
import org.coodex.pojomocker.MAP;
import org.coodex.pojomocker.Relation;
import org.coodex.pojomocker.annotations.INTEGER;
import org.coodex.practice.jaxrs.api.mock.CopyPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class BookInfo {
    @Relation(properties = {"author"}, policy = CopyPolicy.POLICY_NAME)
    private String bookName;
    @Name
//@Relation(properties = {"bookName"}, policy = CopyPolicy.class)
    private String author;
    @INTEGER(min = 100)
    private int price; //分

    public BookInfo() {

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

    @ID
    @IdCard
    @Name
    @VehicleNum
    @DateTime(max = "2020-12-31 23:59:59")
    @EMail(domains = {"github.com", "gmail.com", "coodex.org"})
    @MobilePhoneNum(appleStyle = true)
    @MAP(keyMocker = IdCard.class, valueMocker = MobilePhoneNum.class)
    public Map<String, List<String>> getMap(){
        return new HashMap<>();
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
