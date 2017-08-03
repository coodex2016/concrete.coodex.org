import org.coodex.concrete.jaxrs.Client;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.practice.jaxrs.api.ServiceB;
import org.coodex.practice.jaxrs.api.ServiceExample;
import org.coodex.practice.jaxrs.pojo.Book;
import org.coodex.practice.jaxrs.pojo.BookInfo;
import org.coodex.practice.jaxrs.pojo.GenericPojo;

import java.util.ArrayList;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class JavaClient {

    public static void main(String[] args) {
        try {
            ServiceB test = Client.getInstance(ServiceB.class, "http://localhost:8080");
            ServiceExample serviceExample = Client.getInstance(ServiceExample.class, "http://localhost:8080");

            serviceExample.multiPojo("中文",
                    new ArrayList<int[]>(0),
                    new GenericPojo<BookInfo>(),
                    new Book("管他呢", "不知道", 5000), new int[]{1, 2, 3});
            for (Book book : serviceExample.all()) {
                System.out.println(book);
            }

            serviceExample.findByPriceLessThen(10000);
            serviceExample.findByAuthorLike("神经豆豆");


            serviceExample.get("沈海南是个大逗比", 1005);
            System.out.println(serviceExample.delete(54321));

            serviceExample = Client.getInstance(ServiceExample.class, "http://localhost:8080");

            System.out.println(serviceExample.delete(12345l));
            System.out.println(serviceExample.bigStringTest("中华", "七七八八"));
//
//            SaaSExample saaSExample = Client.getBean(SaaSExample.class, "http://localhost:8081");
//            saaSExample.exampleForSaaS("123456", "OKOKOK");
        } finally {
            ExecutorsHelper.shutdownAllNOW();
        }
    }
}
