package rx.org.coodex.practice.jaxrs.api;

import io.reactivex.Observable;
import org.coodex.concrete.rx.ReactiveExtensionFor;
import org.coodex.practice.jaxrs.api.ServiceExample;
import org.coodex.practice.jaxrs.pojo.*;

import java.util.List;
import java.util.Map;

/**
 * Create by concrete-api-tools.
 */
@ReactiveExtensionFor(ServiceExample.class)
public interface ServiceExample_RX {

    Observable<String> checkRole();

    Observable<Map<String, BookInfo>> genericTest2(Map<String, Book> y);

    Observable<Book> get(String author, long price);

    Observable<GenericPojo<Book>> genericTest5(List<GenericPojo<BookInfo>> gp);

    Observable<String> update(long bookId, BookInfo book);

    Observable<List<String>> genericTest(List<Integer> x);

    Observable<D> genericTest1001(D x);

    Observable<String> bigStringTest(String pathParam, String toPost);

    Observable<List<Book>> all();

    Observable<List<List<BookInfo>>> genericTest3(List<List<BookInfo>> z);

    Observable<Book> get(long bookId);

    Observable<List<Book>> findByAuthorLike(String author);

    Observable<List<Book>> findByPriceLessThen(int price);

    Observable<String> delete(long bookId);

    Observable<Void> multiPojo(String pathParam, List<int[]> body1, GenericPojo<BookInfo> body2, Book body3, int[] body4);

    Observable<String> tokenId();

    Observable<List<D>> genericTest1002(List<D> x);

    Observable<GenericPojo<Book>> genericTest4(GenericPojo<BookInfo> gp);

    Observable<Integer> add(int x, int y);

    Observable<G2<GenericPojo<String>, GenericPojo<Integer>>> g5(G2<GenericPojo<String>, GenericPojo<Integer>> xx);

    Observable<GenericPojo<GenericPojo<Book>>> g6(GenericPojo<GenericPojo<Book>> gp);


}