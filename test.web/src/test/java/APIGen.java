import cc.coodex.concrete.apitools.API;
import cc.coodex.concrete.apitools.jaxrs.jquery.JQueryDocRender;
import cc.coodex.concrete.apitools.jaxrs.jquery.JQueryPromissCodeRender;
import cc.coodex.concrete.apitools.jaxrs.service.ServiceDocRender;
import cc.coodex.concurrent.ExecutorsHelper;
import cc.coodex.practice.jaxrs.api.ServiceExample;

import java.io.IOException;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class APIGen {

    public static void main(String[] args) throws IOException {
        try {
            API.generate(JQueryPromissCodeRender.RENDER_NAME,
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
