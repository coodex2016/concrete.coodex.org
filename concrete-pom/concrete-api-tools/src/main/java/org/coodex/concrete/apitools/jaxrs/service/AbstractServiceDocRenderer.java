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

import org.coodex.concrete.apitools.AbstractRenderer;
import org.coodex.concrete.apitools.jaxrs.DocToolkit;
import org.coodex.concrete.common.ErrorDefinition;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;
import org.coodex.util.Common;

import java.io.IOException;
import java.util.List;

public abstract class AbstractServiceDocRenderer extends AbstractRenderer<JaxrsModule> {
//    protected final DocToolkit toolkit = new ServiceDocToolkit(this);

    protected abstract DocToolkit getToolkit();

    protected void writeModuleList(List<JaxrsModule> modules) throws IOException {
        writeModuleList(modules, null);
    }

    protected void writeModuleList(List<JaxrsModule> modules, String path) throws IOException {
        writeTo((Common.isBlank(path) ? "" : (path + FS)) + "moduleList.md",
                "moduleList.md",
                "modules", modules, getToolkit());
    }

    protected void writeModule(JaxrsModule module) throws IOException {
        writeModule(module, null);
    }

    protected void writeModule(JaxrsModule module, String path) throws IOException {
        writeTo((Common.isBlank(path) ? "" : (path + FS)) + "modules" + FS + module.getInterfaceClass().getName() + ".md",
                "module.md",
                "module", module, getToolkit());
    }

    protected void writeErrorInfo(List<ErrorDefinition> errorDefinitions) throws IOException {
        writeErrorInfo(errorDefinitions, null);
    }

    protected void writeErrorInfo(List<ErrorDefinition> errorDefinitions, String path) throws IOException {
        writeTo((Common.isBlank(path) ? "" : (path + FS)) + "errorInfo.md",
                "errorInfo.md",
                "errorInfo", errorDefinitions);
    }
}
