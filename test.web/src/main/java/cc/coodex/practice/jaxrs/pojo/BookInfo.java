package cc.coodex.practice.jaxrs.pojo;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class BookInfo {
    private String bookName;
    private String author;
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
                "bookName='" + bookName + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                '}';
    }
}
