import org.coodex.concrete.apitools.API;
import org.coodex.concrete.apitools.jaxrs.angular.AngularCodeRender;
import org.coodex.concrete.apitools.jaxrs.jquery.JQueryDocRender;
import org.coodex.concrete.apitools.jaxrs.jquery.JQueryPromisesCodeRender;
import org.coodex.concrete.apitools.jaxrs.service.ServiceDocRender;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.pojomocker.UnableMockException;
import org.coodex.pojomocker.UnsupportedTypeException;
import org.coodex.practice.jaxrs.api.ServiceExample;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class APIGen {

    public static void main(String[] args) throws IOException, UnableMockException, UnsupportedTypeException {
        try {
            Class c = ServiceExample.class;
            Method m = null;
            for(Method method: c.getMethods()){
                if(method.getName().equals("g5")){
                    m = method;
                    break;
                }
            }

//            System.out.println(JSON.toJSONString(POJOMocker.mock(m.getGenericReturnType(), c)));
//            System.out.println(new ServiceDocToolkit(new ServiceDocRender()).formatTypeStr(m.getGenericReturnType(), c));
            API.generate(JQueryPromisesCodeRender.RENDER_NAME,
                    "/concrete-demo/jquery.code",
                    ServiceExample.class.getPackage().getName());

            API.generate(JQueryDocRender.RENDER_NAME,
                    "/concrete-demo/jquery.api",
                    ServiceExample.class.getPackage().getName());
//
            API.generate(ServiceDocRender.RENDER_NAME,
                    "/concrete-demo/restful.api",
                    ServiceExample.class.getPackage().getName());
//
            API.generate(AngularCodeRender.RENDER_NAME,
                    "/concrete-demo/angular.code",
                    ServiceExample.class.getPackage().getName());

            API.generate(AngularCodeRender.RENDER_NAME + ".example",
                    "/concrete-demo/angular.code",
                    ServiceExample.class.getPackage().getName());
        } finally {
            ExecutorsHelper.shutdownAllNOW();
        }
    }
}
