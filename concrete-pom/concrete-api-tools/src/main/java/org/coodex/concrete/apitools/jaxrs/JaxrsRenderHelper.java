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

import org.coodex.concrete.common.modules.AbstractModule;
import org.coodex.concrete.common.modules.Documentable;
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.util.Common;

import java.util.StringJoiner;

public class JaxrsRenderHelper {

    public static String getBody(JaxrsUnit unit) {
        JaxrsParam[] pojoParams = unit.getPojo();
        //            case 1:
        //                return pojoParams[0].getName();
        if (unit.getPojoCount() == 0) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(", ");
//                StringBuilder builder = new StringBuilder("{ ");
        for (JaxrsParam pojoParam : pojoParams) {
//                    if (i > 0) builder.append(", ");
//                    builder.append(pojoParams[i].getName())/*.append(": ").append(pojoParams[i].getName())*/;
            joiner.add(pojoParam.getName() + ": " + pojoParam.getName());
        }
//                builder.append(" }");
//                return builder.toString();
        return "{ " + joiner.toString() + " }";
    }

    private static String getDesc(Documentable documentable) {
        StringBuilder builder = new StringBuilder();
        String name = documentable.getLabel();
        String desc = documentable.getDescription();
        if (!Common.isBlank(name)) {
            builder.append(name);
        }
        if (!Common.isBlank(desc)) {
            if (!Common.isBlank(name)) {
                builder.append(" - ");
            }
            builder.append(desc);
        }
        return builder.toString();
    }

    public static String getDoc(JaxrsUnit unit) {
        StringBuilder builder = new StringBuilder("    /**\n");
        builder.append("     * ").append(getDesc(unit)).append("\n");
        for (JaxrsParam param : unit.getParameters()) {
            builder.append("     * @param {*} ")
                    .append(param.getName())
                    .append(" ").append(getDesc(param)).append('\n');
        }
        builder.append("     * @returns Promise \n");
        builder.append("     */");
        return builder.toString();
    }

    public static String getMethodPath(AbstractModule<JaxrsUnit> module, JaxrsUnit unit) {
        return (module.getName() + unit.getName()).replace("{", "${");
    }
}
