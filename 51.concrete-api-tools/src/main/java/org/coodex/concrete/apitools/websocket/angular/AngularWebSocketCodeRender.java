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

package org.coodex.concrete.apitools.websocket.angular;

import org.coodex.concrete.apitools.AbstractAngularRender;
import org.coodex.concrete.apitools.jaxrs.angular.meta.TSClass;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.struct.AbstractModule;
import org.coodex.concrete.common.struct.AbstractParam;
import org.coodex.concrete.websocket.WebSocketModule;
import org.coodex.concrete.websocket.WebSocketUnit;
import org.coodex.util.Common;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.coodex.concrete.websocket.WebSocketModuleMaker.WEB_SOCKET_SUPPORT;

public class AngularWebSocketCodeRender extends AbstractAngularRender<WebSocketUnit> {

    public static final String RENDER_NAME =
            WEB_SOCKET_SUPPORT + "code.angular.ts.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/websocket/angular/code/v1/";


    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }

    @Override
    public void writeTo(String... packages) throws IOException {
        String moduleName = getRenderDesc().substring(getRenderName().length());
        moduleName = Common.isBlank(moduleName) ? null : moduleName.substring(1);
        List<WebSocketModule> modules = ConcreteHelper.loadModules(getRenderName(), packages);
        String contextPath = Common.isBlank(moduleName) ? "@concrete/" : (getModuleName(moduleName) + "/");

        // 按包归类
        CLASSES.set(new HashMap<String, Map<Class, TSClass>>());
        try {
            for (WebSocketModule module : modules) {
                process(moduleName, module);
            }

            // AbstractConcreteService.ts
            if (!exists(contextPath + "AbstractConcreteService.ts"))
                copyTo("abstractConcreteService.ftl",
                        contextPath + "AbstractConcreteService.ts");

            // packages
            packages(contextPath);

        } finally {
            CLASSES.remove();
        }
    }


    @Override
    protected String getMethodPath(AbstractModule<WebSocketUnit> module, WebSocketUnit unit) {
        return unit.getKey();
    }

    @Override
    protected String getBody(WebSocketUnit unit) {
        switch (unit.getParameters().length) {
            case 0:
                return "{}";
            case 1:
                return unit.getParameters()[0].getName();
            default:
                StringBuilder builder = new StringBuilder("{");
                boolean isFirst = true;
                for (AbstractParam param : unit.getParameters()) {
                    if(!isFirst){
                        builder.append(", ");
                    }
                    builder.append(param.getName()).append(": ").append(param.getName());
                    isFirst = false;
                }
                return builder.append("}").toString();
        }
    }
}
