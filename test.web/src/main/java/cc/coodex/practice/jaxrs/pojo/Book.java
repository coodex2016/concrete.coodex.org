package cc.coodex.practice.jaxrs.pojo;

import cc.coodex.concrete.api.Description;

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
    public long getId() {
        return id;
    }


}
