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

    Observable<List<D>> genericTest1002(List<D> x);

    Observable<G2<GenericPojo<String>, GenericPojo<Integer>>> g5(G2<GenericPojo<String>, GenericPojo<Integer>> xx);

    Observable<String> checkRole();

    Observable<GenericPojo<GenericPojo<Book>>> g6(GenericPojo<GenericPojo<Book>> gp);

    Observable<String> update(long bookId, BookInfo book);

    Observable<Void> multiPojo(String pathParam, List<int[]> body1, GenericPojo<BookInfo> body2, Book body3, int[] body4);

    Observable<String> tokenId();

    Observable<Integer> add(int x, int y);

    Observable<Book> get(String author, long price);

    Observable<List<Book>> findByPriceLessThen(int price);

    Observable<String> delete(long bookId);

    Observable<List<Book>> all();

    Observable<List<Book>> findByAuthorLike(String author);

    Observable<D> genericTest1001(D x);

    Observable<Map<String, BookInfo>> genericTest2(Map<String, Book> y);

    Observable<GenericPojo<Book>> genericTest4(GenericPojo<BookInfo> gp);

    Observable<List<List<BookInfo>>> genericTest3(List<List<BookInfo>> z);

    Observable<GenericPojo<Book>> genericTest5(List<GenericPojo<BookInfo>> gp);

    Observable<Book> get(long bookId);

    Observable<String> bigStringTest(String pathParam, String toPost);

    Observable<List<String>> genericTest(List<Integer> x);


}