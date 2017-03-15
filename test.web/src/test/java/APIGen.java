import org.coodex.concrete.apitools.API;
import org.coodex.concrete.apitools.jaxrs.jquery.JQueryDocRender;
import org.coodex.concrete.apitools.jaxrs.jquery.JQueryPromisesCodeRender;
import org.coodex.concrete.apitools.jaxrs.service.ServiceDocRender;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.practice.jaxrs.api.ServiceExample;

import java.io.IOException;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class APIGen {

    public static void main(String[] args) throws IOException {
        try {
            API.generate(JQueryPromisesCodeRender.RENDER_NAME,
                    "/concrete-demo/jquery.code",
                    ServiceExample.class.getPackage().getName());

            API.generate(JQueryDocRender.RENDER_NAME,
                    "/concrete-demo/jquery.api",
                    ServiceExample.class.getPackage().getName());

            API.generate(ServiceDocRender.RENDER_NAME,
                    "/concrete-demo/restful.api",
                    ServiceExample.class.getPackage().getName());
        } finally {
            ExecutorsHelper.shutdownAllNOW();
        }
    }
}
