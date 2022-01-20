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

package org.coodex.concrete.apitools.jaxrs.angular;

import org.coodex.concrete.apitools.AbstractAngularRenderer;
import org.coodex.concrete.apitools.jaxrs.JaxrsRenderHelper;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.modules.AbstractModule;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.util.Common;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.coodex.concrete.apitools.APIHelper.loadModules;

/**
 * Created by davidoff shen on 2017-04-10.
 */
public class AngularCodeRenderer extends AbstractAngularRenderer<JaxrsModule, JaxrsUnit> {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".code.angular.ts.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/angular/code/v1/";


//    @Override
//    public void writeTo(String... packages) throws IOException {
//        List<JaxrsModule> jaxrsModules = loadModules(RENDER_NAME, packages);
//        render(jaxrsModules);
//    }

    @Override
    public void render(List<JaxrsModule> modules) throws IOException {
        String moduleName = getRenderDesc().substring(RENDER_NAME.length());
        moduleName = Common.isBlank(moduleName) ? null : moduleName.substring(1);

        String contextPath = Common.isBlank(moduleName) ? "@concrete/" : (getModuleName(moduleName) + "/");

        // 按包归类
        CLASSES.set(new HashMap<>());
        try {
            for (JaxrsModule module : modules) {
                process(moduleName, module);
            }

            // AbstractConcreteService.ts
            if (!exists(contextPath + "AbstractConcreteService.ts")) {
                Map<String, Object> versionAndStyle = new HashMap<>();
                versionAndStyle.put("version", ConcreteHelper.VERSION);
//                versionAndStyle.put("style", JaxRSHelper.used024Behavior());

                writeTo(contextPath + "AbstractConcreteService.ts",
                        "abstractConcreteService.ftl",
                        versionAndStyle);
//                copyTo("abstractConcreteService.ftl",
//                        contextPath + "AbstractConcreteService.ts");
            }

            // packages
            packages(contextPath);

        } finally {
            CLASSES.remove();
        }
    }

    @Override
    protected String getModuleType() {
        return "JaxRS";
    }

    @Override
    protected String getMethodPath(AbstractModule<JaxrsUnit> module, JaxrsUnit unit) {
        return JaxrsRenderHelper.getMethodPath(module, unit);
    }

    @Override
    protected String getBody(JaxrsUnit unit) {
        return JaxrsRenderHelper.getBody(unit);
//        Param[] pojoParams = unit.getPojo();
//        switch (unit.getPojoCount()) {
//            case 1:
//                return pojoParams[0].getName();
//            case 0:
//                return null;
//            default:
//                StringBuilder builder = new StringBuilder("{ ");
//                for (int i = 0; i < pojoParams.length; i++) {
//                    if (i > 0) builder.append(", ");
//                    builder.append(pojoParams[i].getName()).append(": ").append(pojoParams[i].getName());
//                }
//                builder.append(" }");
//                return builder.toString();
//        }
    }


    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }
}
