package cc.coodex.practice.jaxrs.api;

import cc.coodex.concrete.api.*;
import cc.coodex.concrete.jaxrs.BigString;
import cc.coodex.practice.jaxrs.pojo.Book;
import cc.coodex.practice.jaxrs.pojo.BookInfo;

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
