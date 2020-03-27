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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.modules.ModuleMaker;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class JaxRSModuleMaker implements ModuleMaker<JaxrsModule> {

    public static final String JAX_RS_PREV = "JaxRS";

    @Override
    public boolean accept(String desc) {
        return desc != null
                && desc.length() >= JAX_RS_PREV.length()
                && JAX_RS_PREV.equalsIgnoreCase(desc.substring(0, JAX_RS_PREV.length()));
    }

//    @Override
//    public boolean isAccept(String desc) {
//        return accept(desc);
//    }

    @Override
    public JaxrsModule make(Class<?> interfaceClass) {
        return new JaxrsModule(interfaceClass);
    }
}
