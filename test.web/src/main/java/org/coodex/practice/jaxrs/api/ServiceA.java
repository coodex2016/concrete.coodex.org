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
