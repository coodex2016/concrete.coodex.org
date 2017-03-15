package org.coodex.practice.jaxrs.api;

import org.coodex.concrete.api.Abstract;
import org.coodex.concrete.api.MicroService;
import org.coodex.practice.jaxrs.pojo.Book;

import java.util.List;

/**
 * Created by davidoff shen on 2016-11-28.
 */
@MicroService
@Abstract
public interface ServiceB extends ServiceA {

    List<Book> all();

    List<Book> findByAuthorLike( String author);

    List<Book> findByPriceLessThen( int price);

}
