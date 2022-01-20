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

import com.alibaba.fastjson.JSON;
import org.coodex.concrete.apitools.jaxrs.DocToolkit;
import org.coodex.concrete.common.ErrorMessageFacade;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;

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

        // package.json todo 版本可设置
        if (!exists("package.json"))
            writeTo("package.json",
                    "{\n" +
                            "  \"name\": \"my-book\",\n" +
                            "  \"version\": \"1.0.0\",\n" +
                            "  \"license\": \"MIT\",\n" +
                            "  \"scripts\": {\n" +
                            "    \"serve\": \"vuepress dev docs\",\n" +
                            "    \"build\": \"vuepress build docs\"\n" +
                            "  },\n" +
                            "  \"devDependencies\": {\n" +
                            "    \"@vuepress/plugin-search\": \"^2.0.0-beta.33\",\n" +
                            "    \"vuepress\": \"^2.0.0-beta.33\"\n" +
                            "  }\n" +
                            "}");
        // .gitignore
        if (!exists(".gitignore"))
            writeTo(".gitignore",
                    "node_modules\n" +
                            ".temp\n" +
                            ".cache\n" +
                            "dist");

        // .vuepress/config.js
        if (!exists("docs/.vuepress/config.js"))
            writeTo("docs/.vuepress/config.js", "const { defineUserConfig } = require(\"@vuepress/cli\");\n" +
                    "\n" +
                    "module.exports = defineUserConfig({\n" +
                    "    title: \"change me\",\n" +
                    "    themeConfig: {\n" +
                    "        sidebar: [\n" +
                    "            {\n" +
                    "                text: '项目简介',\n" +
                    "                link: \"/\"\n" +
                    "            }, {\n" +
                    "                text: 'A. 模块清单',\n" +
//                    "                link: '/modules/',\n" +
                    "                collapsible: true,\n" +
                    "                children: require(\"./modules.json\")\n" +
                    "            }, {\n" +
                    "                text: 'B. 错误信息',\n" +
                    "                link: '/errorInfo',\n" +
                    "            }, {\n" +
                    "                text: 'C. POJO',\n" +
                    "                collapsible: true,\n" +
                    "                children: require(\"./pojos.json\")\n" +
                    "            }\n" +
                    "        ],\n" +
                    "    },\n" +
                    "    plugins: [['@vuepress/plugin-search'],],\n" +
                    "});");

        // 宽度
        if (!exists("docs/.vuepress/styles/index.scss"))
            writeTo("docs/.vuepress/styles/index.scss",
                    ":root{\n" +
                            "    --content-width: 100%;\n" +
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

        writeTo("docs/.vuepress/modules.json", JSON.toJSONString(
                modules.stream()
                        .map(m -> "/modules/" + m.getInterfaceClass().getName())
                        .collect(Collectors.toList())

        ));

        writeTo("docs/.vuepress/pojos.json", JSON.toJSONString(
                toolkit.getPojos().stream().sorted().map(s -> "/pojos/" + s).collect(Collectors.toList())
        ));

    }
}
