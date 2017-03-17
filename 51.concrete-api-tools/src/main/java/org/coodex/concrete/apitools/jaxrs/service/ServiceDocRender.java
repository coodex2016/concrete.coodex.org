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

package org.coodex.concrete.apitools.jaxrs.service;

import org.coodex.concrete.apitools.jaxrs.AbstractRender;
import org.coodex.concrete.apitools.jaxrs.DocToolkit;
import org.coodex.concrete.common.ConcreteToolkit;
import org.coodex.concrete.jaxrs.ErrorDefinition;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.JaxRSServiceHelper;
import org.coodex.concrete.jaxrs.struct.Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class ServiceDocRender extends AbstractRender {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".doc.backend." + "gitbook" + ".v1";

    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/services/doc/gitbook/v1/";

    private final DocToolkit toolkit = new ServiceDocToolkit(this);

    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }


    private void writeSummary(List<Module> modules) throws IOException {
        writeTo("SUMMARY.md",
                "SUMMARY.md",
                "modules", modules, toolkit);
    }

    private void writeModuleList(List<Module> modules) throws IOException {
        writeTo("moduleList.md",
                "moduleList.md",
                "modules", modules, toolkit);
    }

    private void writeModule(Module module) throws IOException {
        writeTo("modules" + FS + toolkit.canonicalName(module.getName()) + ".md",
                "module.md",
                "module", module, toolkit);
    }

    public void writeTo(List<Module> modules) throws IOException {
        // book.json
        if (!exists("book.json"))
            copyTo("book.json", "book.json");

        // README.md
        if (!exists("README.md"))
            copyTo("README.md", "README.md");


        // moduleList.md
        writeModuleList(modules);

        // modules
        for (Module module : modules) {
            writeModule(module);
        }

        // SUMMARY.MD
        writeSummary(modules);

    }

    private void writeErrorInfo(List<ErrorDefinition> errorDefinitions) throws IOException {
        writeTo("errorInfo.md",
                "errorInfo.md",
                "errorInfo", errorDefinitions);
    }

    @Override
    public void writeTo(String... packages) throws IOException {
        writeTo(ConcreteToolkit.<Module>loadModules(RENDER_NAME, packages));

        List<ErrorDefinition> errors = new ArrayList<ErrorDefinition>();

        // write errorInfo
        writeErrorInfo(JaxRSServiceHelper.getAllErrorInfo(packages));
    }
}
