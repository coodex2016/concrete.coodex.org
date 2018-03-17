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

package org.coodex.concrete.apitools.rx;

import org.coodex.concrete.apitools.AbstractRender;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.TypeHelper;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ReactiveStreamsRender extends AbstractRender {

    public static final String RENDER_NAME = "java.code.RxJava2.v1";

    @Override
    protected String getTemplatePath() {
        return "concrete/templates/rx/java/code/v1/";
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }

    @Override
    public void writeTo(String... packages) throws IOException {
        // TODO 建立rx的modulesLoader
        List<Module> modules = ConcreteHelper.loadModules(JaxRSModuleMaker.JAX_RS_PREV + ".loader", packages);

        for (Module module : modules) {
            Map<String, Object> toWrite = new HashMap<String, Object>();
            Set<String> imports = new HashSet<String>();
            toWrite.put("imports", imports);
            Class<?> clazz = module.getInterfaceClass();
            imports.add(clazz.getName());

            String packageName = "rx." + clazz.getPackage().getName();
            String rxClassName = clazz.getSimpleName() + "_RX";
            String outputPath = packageName.replace('.', '/') + '/' + rxClassName + ".java";

            toWrite.put("package", "rx." + clazz.getPackage().getName());
            toWrite.put("concreteClassName", clazz.getSimpleName());
            toWrite.put("rxClassName", rxClassName);

            Set<MethodInfo> methods = new HashSet<MethodInfo>();
            toWrite.put("methods", methods);

            for (Unit unit : module.getUnits()) {
                methods.add(unitToMethod(unit, imports));
            }

            writeTo(outputPath, "rx.java.ftl", toWrite);
        }

    }

    private void addTo(Set<String> imports, String className) {
        try {
            Class c = Class.forName(className);
            if (!c.isPrimitive() && !c.getPackage().getName().equals("java.lang")) {
                imports.add(className);
            }
        } catch (Throwable th) {

        }
    }

    private String typeToStr(Type t, Type context, Set<String> imports, boolean autoBox) {
        t = TypeHelper.toTypeReference(t, context);
        if (t instanceof Class) {
            return classToStr((Class) t, imports, autoBox);
        } else if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            StringBuilder builder = new StringBuilder();

            builder.append(classToStr((Class) pt.getRawType(), imports, false));
            Type[] parameters = pt.getActualTypeArguments();
            builder.append("<");
            for (int i = 0, len = parameters.length; i < len; i++) {
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append(typeToStr(parameters[i], context, imports, false));
            }
            return builder.append(">").toString();
        } else if (t instanceof GenericArrayType) {
            return typeToStr(((GenericArrayType) t).getGenericComponentType(), context, imports, false) + "[]";
        } else
            throw new RuntimeException("unknown TYPE: " + t);
    }

    private String classToStr(Class c, Set<String> imports, boolean autoBox) {
        if (c.isArray()) {
            return classToStr(c.getComponentType(), imports, false) + "[]";
        } else {
            if (void.class.equals(c))
                return "Void";
            else if (autoBox && c.isPrimitive()) {
                return autoBox(c);
            } else {
                String className = c.getName();
                addTo(imports, className);
                return c.getSimpleName();
            }
        }
    }

    private String autoBox(Class c) {
        if (byte.class.equals(c)) {
            return Byte.class.getSimpleName();
        } else if (short.class.equals(c)) {
            return Short.class.getSimpleName();
        } else if (int.class.equals(c)) {
            return Integer.class.getSimpleName();
        } else if (char.class.equals(c)) {
            return Character.class.getSimpleName();
        } else if (long.class.equals(c)) {
            return Long.class.getSimpleName();
        } else if (boolean.class.equals(c)) {
            return Boolean.class.getSimpleName();
        } else if (float.class.equals(c)) {
            return Float.class.getSimpleName();
        } else if (double.class.equals(c)) {
            return Double.class.getSimpleName();
        } else {
            return null;
        }
    }

    private MethodInfo unitToMethod(Unit unit, Set<String> imports) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setName(unit.getFunctionName());
        Class context = unit.getDeclaringModule().getInterfaceClass();
        methodInfo.setReturnType(typeToStr(unit.getGenericReturnType(), context, imports, true));
        for(Param param: unit.getParameters()){
            ParamInfo paramInfo = new ParamInfo();
            paramInfo.setName(param.getName());
            paramInfo.setType(typeToStr(param.getGenericType(), context, imports, false));
            methodInfo.getParams().add(paramInfo);
        }
        return methodInfo;
    }

    public static class MethodInfo {
        private String name;
        private String returnType;
        private List<ParamInfo> params = new ArrayList<ParamInfo>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public List<ParamInfo> getParams() {
            return params;
        }
    }

    public static class ParamInfo {
        private String type;
        private String name;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    public static void main(String[] args) {
        System.out.println(int.class.getSimpleName());
    }
}
