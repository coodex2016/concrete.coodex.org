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

package org.coodex.concrete.jaxrs.saas.javassist;

import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.saas.ReverseResource;
import org.coodex.concrete.support.jsr339.javassist.JSR339ClassGenerator;


/**
 * Created by davidoff shen on 2017-03-22.
 */
public class ReverseClassGenerator extends JSR339ClassGenerator {

    public static final String GENERATOR_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".reverseProxy." + BYTE_CODE_TOOLS_NAME + ".v1";


    @Override
    public boolean isAccept(String desc) {
        return GENERATOR_NAME.equalsIgnoreCase(desc);
    }

    @Override
    public String getImplPostfix() {
        return "$ReverseProxyImpl";
    }

    @Override
    public Class<?> getSuperClass() {
        return ReverseResource.class;
    }

}
