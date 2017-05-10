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

package org.coodex.practice.jaxrs.impl;

import com.alibaba.fastjson.JSON;
import org.coodex.concrete.api.LogAtomic;
import org.coodex.concrete.attachments.client.ClientServiceImpl;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.concrete.jaxrs.BigString;
import org.coodex.practice.jaxrs.api.Calc;
import org.coodex.practice.jaxrs.api.SaaSExample;
import org.coodex.practice.jaxrs.api.ServiceExample;
import org.coodex.practice.jaxrs.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.putLoggingData;


/**
 * Created by davidoff shen on 2016-11-28.
 */
@Named
//@OperationLog(category = "overlay")
public class ServiceExampleImpl implements ServiceExample, Calc, SaaSExample {

    private final static Logger log = LoggerFactory.getLogger(ServiceExampleImpl.class);

    private Token token = TokenWrapper.getInstance();

    private List<Book> books = new ArrayList<Book>();

    public ServiceExampleImpl() {
        books.add(new Book("TSP培训开发团队", "Watts S. Humplhrey", 5900));
        books.add(new Book("从0到1  开启商业与未来的秘密", "Peter Thiel", 4500));
        books.add(new Book("Node.js实战", "Make Cantelon/ Marc Harter/ T. J. Holowarychunk/ Nathan Rajlich", 6900));
    }

    @Override
    public List<Book> all() {
        token.setAttribute("test", "WWWWWWWWWWWWWWWW");
        log.debug("all : tokeId {}", token.getTokenId());
        return books;
    }

    @Override
    public List<Book> findByAuthorLike(String author) {
        List<Book> books = new ArrayList<Book>();
        for (Book book : this.books) {
            if (book.getAuthor().contains(author)) {
                books.add(book);
            }
        }
        return books;
    }

    @Override
    public List<Book> findByPriceLessThen(int price) {
        List<Book> books = new ArrayList<Book>();
        for (Book book : this.books) {
            if (book.getPrice() < price) {
                books.add(book);
            }
        }
        return books;
    }

    @Override
    public Book get(long bookId) {
//        List<Book> books = new ArrayList<>();
        for (Book book : this.books) {
            if (book.getId() == bookId) {
                return book;
            }
        }
        return null;
    }

    @Override
    public String bigStringTest(String pathParam, @BigString String toPost) {
        log.debug("pathParam: {}, toPost: {}", pathParam, toPost);
        return toPost;
    }

    @Override
    @LogAtomic(subClass = "111111")
    public Book get(String author, long price) {
        log.debug("author: {}, price: {}", author, price);
        putLoggingData("logTest", "ok");
        return books.get(0);
    }

    @Override
    public String update(long bookId, BookInfo book) {
        log.debug("bookId: {}, bookInfo: {}", bookId, book);
        return String.valueOf(bookId);
    }

    @Override
    public String delete(long bookId) {
        log.debug("delete book: {} ,tokenId: {}, attr test: {}", bookId, token.getTokenId(), token.getAttribute("test"));
        return String.valueOf(bookId);
    }

    @Override
    public String checkRole() {
        throw new ConcreteException(ErrorCodes.NO_AUTHORIZATION);
    }

    @Override
    public String tokenId() {
        ClientServiceImpl.allowWrite();
        return token.getTokenId();
    }

    @Override
    public List<String> genericTest(List<Integer> x) {
        return null;
    }

    @Override
    public Map<String, BookInfo> genericTest2(Map<String, Book> y) {
        return null;
    }

    @Override
    public List<List<BookInfo>> genericTest3(List<List<BookInfo>> z) {
        return null;
    }

    @Override
    public GenericPojo<Book> genericTest4(GenericPojo<BookInfo> gp) {
        return null;
    }

    @Override
    public GenericPojo<Book> genericTest5(List<GenericPojo<BookInfo>> gp) {
        return null;
    }

    @Override
    public GenericPojo<GenericPojo<Book>> g6(GenericPojo<GenericPojo<Book>> gp) {
        return null;
    }

    @Override
    public void multiPojo(String pathParam, List<int[]> body1, GenericPojo<BookInfo> body2, Book body3, int[] body4) {
        log.debug(pathParam);
        log.debug(JSON.toJSONString(body1));
        log.debug(JSON.toJSONString(body2));
        log.debug(JSON.toJSONString(body3));
        log.debug(JSON.toJSONString(body4));
    }

    //    @Override
    public G2<GenericPojo<String>, GenericPojo<Integer>> g5(G2<GenericPojo<String>, GenericPojo<Integer>> xx) {
        return null;
    }

    @Override
    public int add(int x, int y) {
        return x + y;
    }

    @Override
    public String exampleForSaaS(String tenantId, String ok) {
        log.debug("tenantId: {}", tenantId);
        return ok;
    }

//    @Override
//    public GenericPojo<BookInfo> genericTest1001(GenericPojo<BookInfo> x) {
//        return null;
//    }

    @Override
    public D genericTest1001(D x) {
        return null;
    }

    @Override
    public List<D> genericTest1002(List<D> x) {
        return null;
    }
}
