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

package org.coodex.concrete.apitools.websocket.jquery;

import org.coodex.concrete.apitools.AbstractRenderer;
import org.coodex.concrete.common.modules.AbstractParam;
import org.coodex.concrete.websocket.WebSocketModule;
import org.coodex.concrete.websocket.WebSocketUnit;
import org.coodex.util.Common;

import java.io.IOException;
import java.util.*;

import static org.coodex.concrete.websocket.WebSocketModuleMaker.WEB_SOCKET_SUPPORT;

public class JQueryWebSocketCodeRenderer extends AbstractRenderer<WebSocketModule> {

    public static final String RENDER_NAME =
            WEB_SOCKET_SUPPORT + "code.jquery.js.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/websocket/jquery/code/v1/";

    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }

    private String buildMethod(WebSocketUnit unit) {
        StringBuilder builder = new StringBuilder();
        StringBuilder parameters = new StringBuilder();
        builder.append("function (");
        switch (unit.getParameters().length) {
            case 0:
                parameters.append("undefined");
                break;
            case 1:
                builder.append(unit.getParameters()[0].getName());
                parameters.append(unit.getParameters()[0].getName());
                break;
            default:
                parameters.append("{");
                for (int i = 0; i < unit.getParameters().length; i++) {
                    AbstractParam param = unit.getParameters()[i];
                    if (i > 0) {
                        builder.append(", ");
                        parameters.append(", ");
                    }
                    String n = param.getName();
                    builder.append(n);
                    parameters.append("\"").append(n).append("\": ").append(n);
                }
                parameters.append("}");
        }

        builder.append(") {return invoke({\"serviceId\": \"").append(unit.getKey()).append("\",\"param\": ")
                .append(parameters.toString()).append(" });}");
        return builder.toString();
    }

    private String overload(WebSocketModule module) {
        Map<String, Set<WebSocketUnit>> methods = new HashMap<>();
        for (WebSocketUnit unit : module.getUnits()) {
            String key = unit.getMethod().getName();
            Set<WebSocketUnit> units = methods.computeIfAbsent(key, k -> new HashSet<>());
            units.add(unit);
        }

        StringBuilder builder = new StringBuilder();
        for (String methodName : methods.keySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("\"").append(methodName).append("\": ");

            WebSocketUnit[] units = methods.get(methodName).toArray(new WebSocketUnit[0]);
            if (units.length > 1) {
                // overload
                builder.append("overload(\"").append(methodName).append("\", {");
                for (int i = 0; i < units.length; i++) {
                    if (i > 0) builder.append(", ");
                    builder.append("\"").append(units[i].getParameters().length).append("\": ")
                            .append(buildMethod(units[i]));
                }
                builder.append("})");
            } else {
                builder.append(buildMethod(units[0]));
            }
        }

        return builder.toString();
    }

//    @Override
//    public void writeTo(String... packages) throws IOException {
//        List<WebSocketModule> moduleList = loadModules(RENDER_NAME, packages);
//        render(moduleList);
//    }

    @Override
    public void render(List<WebSocketModule> moduleList) throws IOException {
        Set<String> modules = new HashSet<>();
        for (WebSocketModule module : moduleList) {
            String builder = "register(\"" + module.getInterfaceClass().getSimpleName() + "\", \"" +
                    module.getInterfaceClass().getPackage().getName() + "\", { " +
                    overload(module) +
                    "});";
            modules.add(builder);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("modules", modules);
        String moduleName = getRenderDesc().substring(RENDER_NAME.length());
        moduleName = Common.isBlank(moduleName) ? "concrete" : moduleName.substring(1);
        map.put("moduleName", moduleName);
        writeTo("jquery-websocket-concrete.js", "jquery-websocket-concrete.js.ftl", map);
    }
}
