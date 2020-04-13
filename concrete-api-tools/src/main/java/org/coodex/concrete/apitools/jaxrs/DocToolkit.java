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

import org.coodex.concrete.api.Description;
import org.coodex.concrete.api.Signable;
import org.coodex.concrete.apitools.AbstractRender;
import org.coodex.concrete.core.signature.SignUtil;
import org.coodex.util.Common;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.StringTokenizer;

import static org.coodex.util.GenericTypeHelper.solveFromType;


/**
 * Created by davidoff shen on 2016-12-04.
 */
@SuppressWarnings("unused")
public abstract class DocToolkit {

    private AbstractRender render;

//    public static boolean isPojo(Class<?> type) {
//        return !(isPrimitive(type) ||
//                type.isArray() ||
//                Collection.class.isAssignableFrom(type) ||
//                Map.class.isAssignableFrom(type));
//    }

    public DocToolkit(AbstractRender render) {
        this.render = render;
    }

    public String canonicalName(String name) {
        return canonicalName(name, "\\/");
    }

    public String getPojoName(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public String getPojoPackage(String name) {
        return name.substring(0, name.lastIndexOf('.'));
    }

    public String canonicalName(String name, String delim) {
        StringBuilder builder = new StringBuilder();
        StringTokenizer stringTokenizer = new StringTokenizer(name, delim);
        while (stringTokenizer.hasMoreTokens()) {
            String s = stringTokenizer.nextToken();
            if (Common.isBlank(s)) continue;
            if (builder.length() > 0) builder.append("_");
            builder.append(s);
        }
        return builder.toString();
    }

    public String formatTypeStr(Type t) throws IOException {
        return formatTypeStr(t, null);
    }

    public String formatTypeStr(Type t, Class<?> contextClass) throws IOException {


        if (t instanceof ParameterizedType) {

            ParameterizedType pt = (ParameterizedType) t;
            StringBuilder builder = new StringBuilder();
            builder.append(getClassLabel((Class<?>) pt.getRawType())).append('<');
            boolean isFirst = true;
            for (Type type : pt.getActualTypeArguments()) {
                if (!isFirst)
                    builder.append(", ");
                builder.append(formatTypeStr(type, contextClass));
                isFirst = false;
            }
            builder.append('>');
            return builder.toString();
        } else if (t instanceof TypeVariable) {
            if (contextClass != null) {
                return formatTypeStr(solveFromType((TypeVariable<?>) t, contextClass));
            } else {
                TypeVariable<?> typeVariable = (TypeVariable<?>) t;
                StringBuilder builder = new StringBuilder();
                builder.append(typeVariable.getName());
                if (!Object.class.equals(typeVariable.getBounds()[0])) {
                    builder.append(" extends ").append(formatTypeStr(typeVariable.getBounds()[0]));
                }
                return builder.toString();
            }
        } else if (t instanceof GenericArrayType) {

            return formatTypeStr(((GenericArrayType) t).getGenericComponentType(), contextClass) + "[]";
        } else if (t instanceof Class) {
            if (((Class<?>) t).isArray()) {
                return formatTypeStr(((Class<?>) t).getComponentType(), contextClass) + "[]";
            } else {
                return getClassLabel((Class<?>) t);
            }
        }
        return null;
    }

    protected abstract String getClassLabel(Class<?> clz) throws IOException;

//    public String formatPOJOTypeInfo(POJOTypeInfo info) {
//        if(info.getType() == null){
//            return info.getGenericType().toString();
//        }
//
//        if (info.getType().isArray()) {
//            return formatPOJOTypeInfo(info.getArrayElement()) + "[]";
//        } else {
//            StringBuilder builder = new StringBuilder(getTypeName(info.getType(), info.getContextType()));
////            StringBuilder builder = new StringBuilder(getTypeName(info.getType()));
//            if (info.getGenericParameters().size() > 0) {
//                builder.append("<");
//                boolean isFirst = true;
//                for (POJOTypeInfo param : info.getGenericParameters()) {
//                    if (!isFirst) builder.append(", ");
//                    builder.append(formatPOJOTypeInfo(param));
//                    if (isFirst) {
//                        isFirst = false;
//                    }
//                }
//                builder.append(">");
//            }
//            return builder.toString();
//        }
//    }

//    protected abstract String getTypeName(Class<?> clz, Class<?> contextClass);

    public AbstractRender getRender() {
        return render;
    }

    public String camelCase(String str) {
        return Common.camelCase(str);
    }

    public String tableSafe(String str) {
        return Common.isBlank(str) ? "　" : str;
    }

    public String tableSafeDesc(Description description) {
        return description == null ? "　" : tableSafe(description.description());
    }

    public String tableSafeLabel(Description description) {
        return description == null ? "　" : tableSafe(description.name());
    }

    public String formatSignable(Signable signable) {
        SignUtil.HowToSign howToSign = SignUtil.howToSign(signable);
        String clientKeyId = SignUtil.getString("keyId", howToSign.getPaperName(), null);
        StringBuilder builder = new StringBuilder();
        builder.append("\n\n| property | value |\n| ---- | --- |\n")
                .append("| `paperName` | `").append(howToSign.getPaperName()).append("`|\n ")
                .append("| `algorithm` | `").append(howToSign.getAlgorithm()).append("`|\n ");
        if (clientKeyId != null) {
            builder.append("| `keyId` | `").append(clientKeyId).append("`|\n");
        }
        return builder.toString();
    }


}
