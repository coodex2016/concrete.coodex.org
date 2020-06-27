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

package org.coodex.concrete.apitools.jaxrs.jquery;

import org.coodex.concrete.apitools.AbstractRenderer;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.Polling;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.util.Common;

import java.io.IOException;
import java.util.*;

import static org.coodex.concrete.apitools.APIHelper.loadModules;

/**
 * Created by davidoff shen on 2016-12-04.
 */
public class JQueryPromisesCodeRenderer extends AbstractRenderer {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".code.jquery.js.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/jquery/code/v1/";


    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }

    private String buildMethod(JaxrsUnit unit, JaxrsModule module) {
        StringBuilder builder = new StringBuilder();
        StringBuilder parameters = new StringBuilder();
        builder.append("function (");
        for (int i = 0; i < unit.getParameters().length; i++) {
            JaxrsParam param = unit.getParameters()[i];
            if (i > 0) {
                builder.append(", ");
                parameters.append(", ");
            }
            String n = param.getName();
            builder.append(n);
            parameters.append("\"").append(n).append("\": ").append(n);
        }
        builder.append(") {return invoke({\"path\": \"")
                .append(module.getName()).append(unit.getName()).append("\",\"param\": {")
                .append(parameters.toString()).append("},\"method\": \"")
                .append(unit.getInvokeType()).append("\", \"dataType\": \"")
                .append(String.class.equals(unit.getReturnType()) ? "text" : "json").append("\" });}");
        return builder.toString();
    }

    private String overload(JaxrsModule module) {
        Map<String, Set<JaxrsUnit>> methods = new HashMap<>();
        for (JaxrsUnit unit : module.getUnits()) {
            String key = unit.getMethod().getName();
            Set<JaxrsUnit> units = methods.get(key);
            if (units == null) {
                units = new HashSet<JaxrsUnit>();
                methods.put(key, units);
            }

            units.add(unit);
        }

        StringBuilder builder = new StringBuilder();
        for (String methodName : methods.keySet()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append("\"").append(methodName).append("\": ");

            JaxrsUnit[] units = methods.get(methodName).toArray(new JaxrsUnit[0]);
            if (units.length > 1) {
                // overload
                builder.append("overload(\"").append(methodName).append("\", {");
                for (int i = 0; i < units.length; i++) {
                    if (i > 0) builder.append(", ");
                    builder.append("\"").append(units[i].getParameters().length).append("\": ")
                            .append(buildMethod(units[i], module));
                }
                builder.append("})");
            } else {
                builder.append(buildMethod(units[0], module));
            }
        }

        return builder.toString();
    }

    @Override
    public void writeTo(String... packages) throws IOException {
        Set<String> set = new HashSet<String>(Arrays.asList(packages));
        set.add(Polling.class.getPackage().getName());
        List<JaxrsModule> moduleList = loadModules(RENDER_NAME, set.toArray(new String[0]));

        Set<String> modules = new HashSet<String>();
        for (JaxrsModule module : moduleList) {
            StringBuilder builder = new StringBuilder();
            builder.append("register(\"").append(module.getInterfaceClass().getSimpleName()).append("\", \"")
                    .append(module.getInterfaceClass().getPackage().getName()).append("\", { ")
                    .append(overload(module))
                    .append("});");
            modules.add(builder.toString());
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("modules", modules);
        String moduleName = getRenderDesc().substring(RENDER_NAME.length());
        moduleName = Common.isBlank(moduleName) ? "concrete" : moduleName.substring(1);
        map.put("moduleName", moduleName);
        writeTo("jquery-concrete.js", "jquery-concrete.js.ftl", map);
    }
}
