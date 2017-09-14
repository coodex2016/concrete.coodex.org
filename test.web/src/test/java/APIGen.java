/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.coodex.concrete.apitools.API;
import org.coodex.concrete.apitools.jaxrs.angular.AngularCodeRender;
import org.coodex.concrete.apitools.jaxrs.jquery.JQueryDocRender;
import org.coodex.concrete.apitools.jaxrs.jquery.JQueryPromisesCodeRender;
import org.coodex.concrete.apitools.jaxrs.service.ServiceDocRender;
import org.coodex.concrete.apitools.rx.ReactiveStreamsRender;
import org.coodex.concrete.apitools.websocket.angular.AngularWebSocketCodeRender;
import org.coodex.concrete.apitools.websocket.jquery.JQueryWebSocketCodeRender;
import org.coodex.practice.jaxrs.api.ServiceExample;

import java.io.IOException;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class APIGen {

    public static void main(String[] args) throws IOException {
        API.generate(ReactiveStreamsRender.RENDER_NAME,
                "D:\\Projects\\IntelliJ\\concrete\\test.web\\src\\main\\java",
                ServiceExample.class.getPackage().getName());
        ;
//        try {
//            Class c = ServiceExample.class;
//            Method m = null;
//            for(Method method: c.getMethods()){
//                if(method.getName().equals("g5")){
//                    m = method;
//                    break;
//                }
//            }
//
////            System.out.println(JSON.toJSONString(POJOMocker.mock(m.getGenericReturnType(), c)));
////            System.out.println(new ServiceDocToolkit(new ServiceDocRender()).formatTypeStr(m.getGenericReturnType(), c));
        API.generate(JQueryPromisesCodeRender.RENDER_NAME,
                "/concrete-demo/jquery.code",
                ServiceExample.class.getPackage().getName());

        API.generate(JQueryWebSocketCodeRender.RENDER_NAME,
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
                "D:\\Projects\\front_ends\\ng2-admin\\src",
                ServiceExample.class.getPackage().getName());

        API.generate(AngularWebSocketCodeRender.RENDER_NAME + ".websocket",
                "D:\\Projects\\front_ends\\ng2-admin\\src",
                ServiceExample.class.getPackage().getName());

        API.generate(AngularCodeRender.RENDER_NAME + ".example",
                "/concrete-demo/angular.code",
                ServiceExample.class.getPackage().getName());
//        } finally {
//            ExecutorsHelper.shutdownAllNOW();
//        }
    }
}
