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

package org.coodex.concrete.apitools.jaxrs.axios;

import org.coodex.concrete.apitools.AbstractRender;
import org.coodex.concrete.apitools.jaxrs.JaxrsRenderHelper;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.Common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AxiosCodeRender extends AbstractRender {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".code.axios.js.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/axios/code/v1/";

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
        String moduleName = getRenderDesc().substring(RENDER_NAME.length());
        moduleName = Common.isBlank(moduleName) ? "concrete" : moduleName.substring(1);
        List<Module> moduleList = ConcreteHelper.loadModules(RENDER_NAME, packages);
        writeTo("jaxrs/concrete.js", "concrete.ftl", "version", (Object) ConcreteHelper.VERSION);
        for (Module module : moduleList) {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("moduleName", moduleName);
            param.put("serviceName", module.getInterfaceClass().getSimpleName());

            Map<String,Map<String,Object>> methods = new HashMap<String,Map<String,Object>>();

            for(Unit unit : module.getUnits()){
                String methodName = unit.getMethod().getName();
                Map<String, Object> method = methods.get(methodName);
                if(method == null){
                    method = new HashMap<String, Object>();
                    method.put("name", methodName);
                    methods.put(methodName,method);
                }
                List<Map<String, Object>> overloads = (List<Map<String, Object>>) method.get("overloads");
                if(overloads == null){
                    overloads = new ArrayList<Map<String, Object>>();
                    method.put("overloads", overloads);
                }
                Map<String, Object> overload = new HashMap<String, Object>();
                overloads.add(overload);
                List<String> params = new ArrayList<String>();
                for(Param p: unit.getParameters()){
                    params.add(p.getName());
                }
                overload.put("paramCount", unit.getParameters().length);
                overload.put("params", params);
                overload.put("body", JaxrsRenderHelper.getBody(unit));
                overload.put("url", JaxrsRenderHelper.getMethodPath(module, unit));
                overload.put("resultType",String.class.equals(unit.getReturnType()) ? "text" : "json");
                overload.put("httpMethod", unit.getInvokeType());


            }
            param.put("methods", methods.values());

            writeTo("jaxrs/" + moduleName + "/" + module.getInterfaceClass().getName() + ".js",
                    "service.ftl", param);
        }
    }

}
