/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.test.client;

import org.coodex.concrete.apitools.API;
import org.coodex.concrete.apitools.jaxrs.jquery.JQueryPromisesCodeRender;
import org.coodex.concrete.test.api.Test;
import org.coodex.util.Profile;

import java.io.IOException;

public class WTF {


    public static void main(String[] args) throws IOException {
//        String basePath = Profile.getProfile("env.properties").getString("path");
//        Map<String, String> renders = new HashMap<String, String>();
//        renders.put(AngularCodeRender.RENDER_NAME, "ng2v1");
//        renders.put(AngularCodeRenderV2.RENDER_NAME, "ng2v2");
//        renders.put(AngularWebSocketCodeRender.RENDER_NAME, "ng2ws");
//        renders.put(JQueryPromisesCodeRender.RENDER_NAME, "jquery");
//        renders.put(JQueryWebSocketCodeRender.RENDER_NAME, "jquery-ws");
//        renders.put(JQueryDocRender.RENDER_NAME,"jquery-doc");
//        renders.put(ServiceDocRender.RENDER_NAME,"service-doc");
//        renders.put(ReactiveStreamsRender.RENDER_NAME, "rx");
//
//        for(String render: renders.keySet()){
//            API.generate(render, basePath + renders.get(render), Test.class.getPackage().getName());
//        }

        API.generate(JQueryPromisesCodeRender.RENDER_NAME,
                Profile.getProfile("env.properties").getString("path.jquery.test"),
                Test.class.getPackage().getName());


    }
}
