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
import org.coodex.concrete.apitools.jaxrs.DocToolkit;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;

import java.io.IOException;
import java.util.List;

import static org.coodex.concrete.apitools.APIHelper.loadModules;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class JQueryDocRenderer extends AbstractRenderer {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".doc.jquery.gitbook.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/jquery/doc/v1/";

    private DocToolkit toolkit = new JQueryDocToolkit(this);

    private void writeSummary(List<JaxrsModule> modules) throws IOException {
        writeTo("SUMMARY.md",
                "SUMMARY.md",
                "modules", modules, toolkit);
    }

    private void writeModuleList(List<JaxrsModule> modules) throws IOException {
        writeTo("moduleList.md",
                "moduleList.md",
                "modules", modules, toolkit);
    }

    private void writeModule(JaxrsModule module) throws IOException {
        writeTo("modules" + FS + module.getInterfaceClass().getName() + ".md",
                "module.md",
                "module", module, toolkit);
    }

    @Override
    public void writeTo(String... packages) throws IOException {
        List<JaxrsModule> modules = loadModules(RENDER_NAME, packages);


        // book.json
        if (!exists("book.json"))
            copyTo("book.json", "book.json");

        // README.md
        if (!exists("README.md"))
            copyTo("README.md", "README.md");


        // moduleList.md
        writeModuleList(modules);

        // modules
        for (JaxrsModule module : modules) {
            writeModule(module);
        }

        // SUMMARY.MD
        writeSummary(modules);
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
