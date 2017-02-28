import cc.coodex.concrete.jaxrs.Client;
import cc.coodex.concurrent.ExecutorsHelper;
import cc.coodex.practice.jaxrs.api.ServiceExample;
import cc.coodex.practice.jaxrs.pojo.Book;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class JavaClient {

    public static void main(String[] args) {
        try {
            ServiceExample serviceExample = Client.getBean(ServiceExample.class);
            for (Book book : serviceExample.all()) {
                System.out.println(book);
            }


            serviceExample.get("沈海南是个大逗比", 1005);
            System.out.println(serviceExample.delete(54321));

            serviceExample = Client.getBean(ServiceExample.class, "http://localhost:8080/s");

            System.out.println(serviceExample.delete(12345l));
        }finally {
            ExecutorsHelper.shutdownAllNOW();
        }
    }
}
