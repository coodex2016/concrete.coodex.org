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

package org.coodex.concrete.apitools.jaxrs;

import org.coodex.concrete.common.struct.AbstractModule;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;

public class JaxrsRenderHelper {

    public static String getBody(Unit unit){
        Param[] pojoParams = unit.getPojo();
        switch (unit.getPojoCount()) {
            case 1:
                return pojoParams[0].getName();
            case 0:
                return null;
            default:
                StringBuilder builder = new StringBuilder("{ ");
                for (int i = 0; i < pojoParams.length; i++) {
                    if (i > 0) builder.append(", ");
                    builder.append(pojoParams[i].getName()).append(": ").append(pojoParams[i].getName());
                }
                builder.append(" }");
                return builder.toString();
        }
    }

    public static String getMethodPath(AbstractModule<Unit> module, Unit unit) {
        return (module.getName() + unit.getName()).replace("{", "${");
    }
}
