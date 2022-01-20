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

package org.coodex.concrete.apitools.jaxrs.service;

import org.coodex.concrete.apitools.jaxrs.DocToolkit;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;

import java.io.IOException;
import java.util.List;

import static org.coodex.concrete.common.ErrorMessageFacade.getAllErrorInfo;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class ServiceDocRenderer extends AbstractServiceDocRenderer {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".doc.backend.gitbook.v1";

    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/services/doc/gitbook/v1/";

    private DocToolkit toolkit;

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


    private void writeSummary(List<JaxrsModule> modules) throws IOException {
        writeTo("SUMMARY.md",
                "SUMMARY.md",
                "modules", modules, toolkit);
    }

    public void writeTo(List<JaxrsModule> modules) throws IOException {
        // book.json
        if (!exists("book.json")) {
            copyTo("book.json", "book.json");
        }

        // README.md
        if (!exists("README.md")) {
            copyTo("README.md", "README.md");
        }


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
    public void render(List<JaxrsModule> modules) throws IOException {
        toolkit = new ServiceDocToolkit(this);
        writeTo(modules);
        writeErrorInfo(getAllErrorInfo());
    }

//    @Override
//    public void writeTo(String... packages) throws IOException {
//        render(loadModules(RENDER_NAME,packages));
////        writeTo(loadModules(RENDER_NAME, packages));
////
//////        List<ErrorDefinition> errors = new ArrayList<ErrorDefinition>();
////
////        // write errorInfo
////        writeErrorInfo(getAllErrorInfo());
//    }
}
