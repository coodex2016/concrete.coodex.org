/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.apitools.jaxrs.service;

import org.coodex.concrete.apitools.jaxrs.DocToolkit;
import org.coodex.concrete.common.ErrorMessageFacade;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;
import org.coodex.util.JSONSerializer;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class VuePressDocRenderer extends AbstractServiceDocRenderer {


    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".doc.backend.vuepress.v1";

    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/services/doc/vuepress/v1/";

    private ServiceDocToolkit toolkit;

    @Override
    protected DocToolkit getToolkit() {
        return toolkit;
    }

    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }


    @Override
    public void render(List<JaxrsModule> modules) throws IOException {

        // package.json.txt
        if (!exists("package.json.txt"))
            copyTo("package.json.txt", "package.json");
        // .gitignore
        if (!exists(".gitignore"))
            writeTo(".gitignore",
                    "node_modules\n" +
                            ".temp\n" +
                            ".cache\n" +
                            "dist");

        // .vuepress/config.ts.ftl
        if (!exists("docs/.vuepress/config.ts"))
            copyTo("config_ts.ftl", "docs/.vuepress/config.ts");


        // 宽度
        if (!exists("docs/.vuepress/styles/index.scss"))
            writeTo("docs/.vuepress/styles/index.scss",
                    ":root{\n" +
                            "    --content-width: 960px;\n" +
                            "}");

        // README.md
        if (!exists("docs/README.md")) {
            copyTo("README.md", "docs/README.md");
        }

        toolkit = new ServiceDocToolkit(this, "docs");
        // moduleList.md
        writeModuleList(modules, "docs");

        // modules
        for (JaxrsModule module : modules) {
            writeModule(module, "docs");
        }

        writeErrorInfo(ErrorMessageFacade.getAllErrorInfo(), "docs");

        writeTo("docs/.vuepress/modules.json", JSONSerializer.getInstance().toJson(
                modules.stream()
                        .map(m -> "/modules/" + m.getInterfaceClass().getName())
                        .collect(Collectors.toList())
        ));

        writeTo("docs/.vuepress/pojos.json", JSONSerializer.getInstance().toJson(
                toolkit.getPojos().stream().sorted().map(s -> "/pojos/" + s).collect(Collectors.toList())
        ));

    }
}
