package cc.coodex.practice.jaxrs.pojo;

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

    public long getId() {
        return id;
    }


}
