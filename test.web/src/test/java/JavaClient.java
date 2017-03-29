import org.coodex.concrete.jaxrs.Client;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.practice.jaxrs.api.SaaSExample;
import org.coodex.practice.jaxrs.api.ServiceExample;
import org.coodex.practice.jaxrs.pojo.Book;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class JavaClient {

    public static void main(String[] args) {
        try {
            ServiceExample serviceExample = Client.getBean(ServiceExample.class, "http://localhost:8080");
            for (Book book : serviceExample.all()) {
                System.out.println(book);
            }


            serviceExample.get("沈海南是个大逗比", 1005);
            System.out.println(serviceExample.delete(54321));

            serviceExample = Client.getBean(ServiceExample.class, "http://localhost:8080");

            System.out.println(serviceExample.delete(12345l));
            System.out.println(serviceExample.bigStringTest("中华", "七七八八"));

            SaaSExample saaSExample = Client.getBean(SaaSExample.class, "http://localhost:8081");
            saaSExample.exampleForSaaS("123456", "OKOKOK");
        }finally {
            ExecutorsHelper.shutdownAllNOW();
        }
    }
}
