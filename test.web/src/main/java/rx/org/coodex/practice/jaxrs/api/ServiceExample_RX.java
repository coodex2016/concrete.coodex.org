package rx.org.coodex.practice.jaxrs.api;

import org.coodex.concrete.rx.ReactiveExtensionFor;

import io.reactivex.Observable;

import java.util.List;
import java.util.Map;
import org.coodex.practice.jaxrs.api.ServiceExample;
import org.coodex.practice.jaxrs.pojo.Book;
import org.coodex.practice.jaxrs.pojo.BookInfo;
import org.coodex.practice.jaxrs.pojo.D;
import org.coodex.practice.jaxrs.pojo.G2;
import org.coodex.practice.jaxrs.pojo.GenericPojo;

/**
 * Create by concrete-api-tools.
 */
@ReactiveExtensionFor(ServiceExample.class)
public interface ServiceExample_RX {

    Observable<Book> get(String author, long price);

    Observable<String> tokenId();

    Observable<G2<GenericPojo<String>, GenericPojo<Integer>>> g5(G2<GenericPojo<String>, GenericPojo<Integer>> xx);

    Observable<String> delete(long bookId);

    Observable<Map<String, BookInfo>> genericTest2(Map<String, Book> y);

    Observable<List<D>> genericTest1002(List<D> x);

    Observable<List<Book>> findByPriceLessThen(int price);

    Observable<String> update(long bookId, BookInfo book);

    Observable<Integer> add(int x, int y);

    Observable<GenericPojo<GenericPojo<Book>>> g6(GenericPojo<GenericPojo<Book>> gp);

    Observable<D> genericTest1001(D x);

    Observable<String> bigStringTest(String arg0, String arg1);

    Observable<String> checkRole();

    Observable<List<List<BookInfo>>> genericTest3(List<List<BookInfo>> z);

    Observable<GenericPojo<Book>> genericTest4(GenericPojo<BookInfo> gp);

    Observable<List<String>> genericTest(List<Integer> x);

    Observable<Void> multiPojo(String pathParam, List<int[]> body1, GenericPojo<BookInfo> body2, Book body3, int[] body4);

    Observable<Book> get(long bookId);

    Observable<List<Book>> all();

    Observable<GenericPojo<Book>> genericTest5(List<GenericPojo<BookInfo>> gp);

    Observable<List<Book>> findByAuthorLike(String author);


}