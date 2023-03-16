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

package org.coodex.concrete.apitools.jaxrs.axios;

import org.coodex.concrete.api.Description;
import org.coodex.concrete.apitools.AbstractRenderer;
import org.coodex.concrete.apitools.jaxrs.EnumElementInfo;
import org.coodex.concrete.apitools.jaxrs.JaxrsRenderHelper;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.concrete.own.OwnServiceUnit;
import org.coodex.util.Common;
import org.coodex.util.PojoInfo;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.coodex.concrete.apitools.jaxrs.axios.TsDefineHelper.ifEnum;
import static org.coodex.concrete.common.ConcreteHelper.isPrimitive;
import static org.coodex.util.Common.cast;

public class AxiosCodeRenderer extends AbstractRenderer<JaxrsModule> {

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".code.axios.js.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/axios/code/v1/";
    private boolean constUtilExist = false;

    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }

//    @Override
//    public void writeTo(String... packages) throws IOException {
//        List<JaxrsModule> moduleList = loadModules(RENDER_NAME, packages);
//        render(moduleList);
//    }

    private void processEnum(JaxrsUnit unit, Set<Type> processed) {
        // 1.返回值
        processEnum(unit.getGenericReturnType(), processed);

        for (JaxrsParam param : unit.getParameters()) {
            processEnum(param.getGenericType(), processed);
        }
    }

    private void processEnum(Type t, Set<Type> processed) {
        if (processed.contains(t)) return;
        processed.add(t);

        if (t instanceof ParameterizedType) {

            ParameterizedType pt = (ParameterizedType) t;
            processEnum(pt.getRawType(), processed);
            for (Type type : pt.getActualTypeArguments()) {
                processEnum(type, processed);
            }

        } else if (t instanceof TypeVariable) {

        } else if (t instanceof GenericArrayType) {
            processEnum(((GenericArrayType) t).getGenericComponentType(), processed);
        } else if (t instanceof Class) {
            Class<?> c = (Class<?>) t;
            if (c.isArray()) {
                processEnum(c.getComponentType(), processed);
            } else if (c.isEnum()) {
                Map<String, Object> map = new HashMap<>();
                map.put("elements", EnumElementInfo.of(Common.cast(c)));
                try {
                    writeTo(
                            "jaxrs/constants/" + c.getName() + ".js",
                            "enumerable.ftl",
                            map
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // for d.ts
                map.put("enumTypeName", c.getSimpleName());
                map.put("valueType", TsDefineHelper.getTypeScriptValueType(Common.cast(c)));
                try {
                    writeTo("jaxrs/constants/" + c.getName() + ".d.ts",
                            "enumerable.d.ts.ftl",
                            map);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (!isPrimitive(c) && !c.getPackage().getName().startsWith("java")) {
                PojoInfo pojoInfo = new PojoInfo(c);
                pojoInfo.getProperties().forEach(pojoProperty -> {
                    processEnum(pojoProperty.getType(), processed);
                });
            }
        }
    }

    private void processEnum(JaxrsModule module, Set<Type> processed) throws IOException {
        if (!constUtilExist) {
            copyTo("EnumBase.d.ts.ftl", "jaxrs/constants/EnumBase.d.ts");
            writeTo("jaxrs/constants/constUtil.js", "const allValues = c => {\n" +
                    "    var a = []\n" +
                    "    for (var k in c) {\n" +
                    "        if (typeof c[k] === 'function') continue\n" +
                    "        a.push(c[k])\n" +
                    "    }\n" +
                    "    return a\n" +
                    "}\n" +
                    "\n" +
                    "export function valueOf(c, value) {\n" +
                    "    for (var k in c) {\n" +
                    "        if (typeof c[k] === 'function') continue\n" +
                    "        if (c[k] === value) {\n" +
                    "            return {\n" +
                    "                key: k,\n" +
                    "                value: c[k],\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "    return null\n" +
                    "}\n" +
                    "\n" +
                    "export function toArray(c, values) {\n" +
                    "    var result = []\n" +
                    "    if (!values) values = allValues(c)\n" +
                    "    for (var i = 0, l = values.length; i < l; i++) {\n" +
                    "        var v = valueOf(c, values[i])\n" +
                    "        if (v) result.push(v)\n" +
                    "    }\n" +
                    "    return result\n" +
                    "}");

            constUtilExist = true;
        }
        for (JaxrsUnit unit : module.getUnits()) {
            processEnum(unit, processed);
        }
    }

    @Override
    public void render(List<JaxrsModule> modules) throws IOException {
        constUtilExist = false;
        String moduleName = getModuleName();

        Map<String, Object> versionAndStyle = new HashMap<>();
        versionAndStyle.put("version", ConcreteHelper.VERSION);
//        versionAndStyle.put("style", JaxRSHelper.used024Behavior());

        writeTo("jaxrs/concrete.js", "concrete.ftl", versionAndStyle);
        copyTo("concrete.d.ts.ftl", "jaxrs/concrete.d.ts");
        Set<Type> processed = new HashSet<>();
        for (JaxrsModule module : modules) {
            processEnum(module, processed);
            Map<String, Object> param = new HashMap<>();
            param.put("moduleName", moduleName);
            param.put("serviceName", module.getInterfaceClass().getSimpleName());

            Map<String, Map<String, Object>> methods = new HashMap<>();

            for (JaxrsUnit unit : module.getUnits()) {
                String methodName = unit.getMethod().getName();

                Map<String, Object> method = methods.get(methodName);
                if (method == null) {
                    method = new HashMap<>();
                    method.put("name", methodName);
                    method.put("serviceId", OwnServiceUnit.getUnitKey(unit));
                    methods.put(methodName, method);
                }

                method.put("jsdoc", JaxrsRenderHelper.getDoc(unit));

                List<Map<String, Object>> overloads = cast(method.get("overloads"));
                if (overloads == null) {
                    overloads = new ArrayList<>();
                    method.put("overloads", overloads);
                }
                Map<String, Object> overload = new HashMap<>();
                overloads.add(overload);
                List<String> params = new ArrayList<>();
                for (JaxrsParam p : unit.getParameters()) {
                    params.add(p.getName());
                }
//                overload.put("methodParamCount", unit.getMethod().getParameterCount());
                overload.put("paramCount", unit.getParameters().length);
                overload.put("params", params);
                overload.put("body", JaxrsRenderHelper.getBody(unit));
                overload.put("url", JaxrsRenderHelper.getMethodPath(module, unit));
                overload.put("resultType", String.class.equals(unit.getReturnType()) ? "text" : "json");
                overload.put("httpMethod", unit.getInvokeType());

                if (overloads.size() > 1) {
                    param.put("overloadUsed", true);
                }
            }
            param.put("methods", methods.values());
            writeTo("jaxrs/" + moduleName + "/" + module.getInterfaceClass().getName() + ".js",
                    "service.ftl", param);

            genDTS(module);
        }

    }

    private String getModuleName() {
        String moduleName = getRenderDesc().substring(RENDER_NAME.length());
        moduleName = Common.isBlank(moduleName) ? "concrete" : moduleName.substring(1);
        return moduleName;
    }


    private void genDTS(JaxrsModule module) throws IOException {
        Map<String, Object> param = new HashMap<>();
        param.put("serviceName", module.getInterfaceClass().getSimpleName());

        List<TsDefineHelper.TsType> allTypes = new ArrayList<>();
        Class<?> contextClass = module.getInterfaceClass();
        Function<Type, TsDefineHelper.TsType> to = t -> {
            TsDefineHelper.TsType resultType = TsDefineHelper.javaToTs(t, contextClass);
            allTypes.add(resultType);
            return resultType;
        };


        List<Map<String, Object>> methods = new ArrayList<>();
        for (JaxrsUnit unit : module.getUnits()) {
            List<String> methodSign = new ArrayList<>();
            Map<String, Object> method = new HashMap<>();
            List<String> lines = new ArrayList<>();
            Optional.ofNullable(unit.getDeclaredAnnotation(Description.class))
                    .ifPresent(desc -> {
                        lines.add(" * " + desc.name());
                        if (Common.isBlank(desc.description())) return;
                        lines.add(" * " + desc.description());
                    });
            Set<String> enumSee = new HashSet<>();
            Consumer<Class<Enum<?>>> consumer = (c) -> enumSee.add(c.getCanonicalName());
            ifEnum(unit.getGenericReturnType(), consumer);
            for (JaxrsParam p : unit.getParameters()) {
                lines.add(" * @param " + p.getName() + " " + p.getLabel() + " " + p.getDescription());
                methodSign.add(p.getName() + "?: " + to.apply(p.getGenericType()).toText());
                ifEnum(p.getGenericType(), consumer);
            }
            Optional.ofNullable(unit.getDeclaredAnnotation(Deprecated.class))
                    .ifPresent(d -> lines.add(" * @deprecated"));
            StringBuilder methodDTS = new StringBuilder(unit.getMethod().getName()).append("(")
                    .append(String.join(", ", methodSign))
                    .append("): Promise<").append(to.apply(unit.getGenericReturnType()).toText()).append(">;");
            enumSee.stream().sorted().forEach(s -> lines.add(" * @see " + s));
            if (!lines.isEmpty()) {
                lines.add(0, "/**");
                lines.add(" */");
            }
            method.put("lines", lines);
            method.put("def", methodDTS);

            methods.add(method);
        }
        param.put("methods", methods);
        param.put("declaredTypes", TsDefineHelper.toTypeScriptDef(allTypes));

        writeTo("jaxrs/" + getModuleName() + "/" + module.getInterfaceClass().getName() + ".d.ts",
                "service.d.ts.ftl", param);

    }

}
