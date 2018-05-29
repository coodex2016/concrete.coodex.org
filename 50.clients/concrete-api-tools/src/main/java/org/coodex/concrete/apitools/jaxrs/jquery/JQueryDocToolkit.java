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

import com.alibaba.fastjson.JSON;
import org.coodex.concrete.apitools.AbstractRender;
import org.coodex.concrete.apitools.jaxrs.service.ServiceDocToolkit;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.util.Common;

/**
 * Created by davidoff shen on 2016-12-05.
 */
public class JQueryDocToolkit extends ServiceDocToolkit {

    public JQueryDocToolkit(AbstractRender render) {
        super(render);
    }

    public String camelCase(String s) {
        return Common.camelCase(s);
    }

    public String mockParameters(Unit unit, Module module) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < unit.getParameters().length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            try {
                builder.append(
                        JSON.toJSONString(
                                MockerFacade.mock(
                                        unit.getParameters()[i].getGenericType(), module.getInterfaceClass()
                                ), true));
            } catch (Throwable e) {
                builder.append("{}");
            }
        }
        return builder.toString();
    }
}
