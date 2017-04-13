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

package org.coodex.concrete.apitools.jaxrs.angular;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.apitools.jaxrs.AbstractRender;
import org.coodex.concrete.apitools.jaxrs.angular.meta.*;
import org.coodex.concrete.common.ConcreteToolkit;
import org.coodex.concrete.jaxrs.JaxRSModuleMaker;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.Common;
import org.coodex.util.TypeHelper;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import static org.coodex.concrete.jaxrs.JaxRSHelper.getSubmitBody;

/**
 * Created by davidoff shen on 2017-04-10.
 */
public class AngularCodeRender extends AbstractRender {

    //    static String [] keyWords = {"number", "new", "public", "private", "protected", "static", "string", };

    public static final String RENDER_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".code.angular.ts.v1";
    private static final String RESOURCE_PACKAGE = "concrete/templates/jaxrs/angular/code/v1/";

    private static final ThreadLocal<Map<String, Map<Class, TSClass>>> CLASSES = new ThreadLocal<Map<String, Map<Class, TSClass>>>();

    private String getModuleName(String moduleName) {
        return moduleName.charAt(0) == '@' ? moduleName : ("@" + moduleName);
    }

    @Override
    public void writeTo(String... packages) throws IOException {
        String moduleName = getRenderDesc().substring(RENDER_NAME.length());
        moduleName = Common.isBlank(moduleName) ? null : moduleName.substring(1);
        List<Module> jaxrsModules = ConcreteToolkit.loadModules(RENDER_NAME, packages);
        String contextPath = Common.isBlank(moduleName) ? "@concrete/" : (getModuleName(moduleName) + "/");

        // 按包归类
        CLASSES.set(new HashMap<String, Map<Class, TSClass>>());
        try {
            for (Module module : jaxrsModules) {
                process(moduleName, module);
            }

            // AbstractConcreteService.ts
            if (!exists(contextPath + "AbstractConcreteService.ts"))
                copyTo("abstractConcreteService.ftl",
                        contextPath + "AbstractConcreteService.ts");

            // packages
            for (String key : getClasses().keySet()) {
                Map<Class, TSClass> map = getClasses().get(key);
                Map<String, Object> toWrite = new HashMap<String, Object>();
                toWrite.put("contextPath", contextPath);
                Set<Class> classSet = new HashSet<Class>();
                for (Class clz : map.keySet()) {
                    if (ConcreteService.class.isAssignableFrom(clz))
                        toWrite.put("includeServices", Boolean.TRUE);
                    classSet.addAll(map.get(clz).getImports());
                }

                Map<String, TSImport> imports = new HashMap<String, TSImport>();
                for (Class clz : classSet) {
                    String packageName = clz.getPackage().getName().replace('.', '/');
                    if (key.equals(packageName)) continue;
                    TSImport importSet = imports.get(packageName);
                    if (importSet == null) {
                        importSet = new TSImport();
                        importSet.setPackageName(packageName);
                        imports.put(packageName, importSet);
                    }
                    importSet.getClasses().add(clz.getSimpleName());
                }
                toWrite.put("imports", imports.values());

                toWrite.put("classes", sort(map));
                writeTo(contextPath + key + ".ts",
                        "tspackage.ftl",
                        toWrite);
            }

        } finally {
            CLASSES.remove();
        }
    }

    private Collection<TSClass> sort(Map<Class, TSClass> classes) {
        List<TSClass> ordered = new ArrayList<TSClass>();
        Map<Class, TSClass> cache = new HashMap<Class, TSClass>(classes);
        while (cache.keySet().size() > 0) {
            Class[] keys = cache.keySet().toArray(new Class[0]);
            for (Class key : keys) {
                TSClass tsClass = cache.get(key);
                if (noDep(tsClass, cache)) {
                    cache.remove(key);
                    ordered.add(tsClass);
                }
            }
        }
        return ordered;
    }

    private boolean noDep(TSClass tsClass, Map<Class, TSClass> cache) {
        for (Class clz : tsClass.getImports()) {
            if (cache.get(clz) != null) return false;
        }
        return true;
    }

    private void process(String moduleName, Module module) {
        Class<?> clz = module.getInterfaceClass();
        Map<Class, TSClass> moduleMap = getTSClassMap(clz);
        if (moduleMap == null) return;

        TSModule tsModule = new TSModule(clz);
        tsModule.setBelong(moduleName);

        Set<String> methods = new HashSet<String>();
        for (Unit unit : module.getUnits()) {

            TSMethod method = new TSMethod();

            method.setName(getMethodName(unit.getMethod().getName(), methods));
            method.setHttpMethod(unit.getInvokeType());
            method.setReturnType(getClassType(unit.getGenericReturnType(), tsModule, clz));
            method.setMethodPath(
                    (module.getName() + unit.getName()).replace("{", "${"));

            Param toSubmit = getSubmitBody(unit);
            if (toSubmit != null)
                method.setBody(toSubmit.getName());
//            JaxRSHelper.isBigString(toSubmit) ?
//                    // TODO: 待验证，不确定
//                    String.format("{ %s }", toSubmit.getName()) : toSubmit.getName()

            //params
            method.setParams(getParams(unit, tsModule));

            tsModule.getMethods().add(method);
        }
        moduleMap.put(clz, tsModule);
    }


    /// TODO: 处理方式不妥。如果Service method定义变换顺序后，会导致基于原版本的code全部出问题
    private String getMethodName(String name, Set<String> methods) {
        String methodName = name;
        int prefix = 0;
        while (methods.contains(methodName)) {
            methodName = name + prefix++;
        }
        methods.add(methodName);
        return methodName;
    }

    private Map<String, Map<Class, TSClass>> getClasses() {
        return CLASSES.get();
    }

    private List<TSParam> getParams(Unit unit, TSClass clz) {
        List<TSParam> fieldList = new ArrayList<TSParam>();
        for (int i = 0; i < unit.getParameters().length; i++) {
            Param param = unit.getParameters()[i];
            TSParam field = new TSParam();
            field.setName(param.getName());
            field.setType(getClassType(param.getGenericType(), clz, unit.getDeclaringModule().getInterfaceClass()));
            fieldList.add(field);
        }
        return fieldList;
    }


    private Map<Class, TSClass> getTSClassMap(Class<?> clz) {
        Map<String, Map<Class, TSClass>> classes = getClasses();
        String packageName = clz.getPackage().getName().replace('.', '/');
        Map<Class, TSClass> moduleMap = classes.get(packageName);
        if (moduleMap == null) {
            moduleMap = new HashMap<Class, TSClass>();
            classes.put(packageName, moduleMap);
        }
        return moduleMap;
    }


    private static final Class[] NUMBERS = new Class[]{
            byte.class, int.class, short.class, long.class, float.class, double.class
    };

    private String getClassType(Type type, TSClass clz, Class contextClass) {
        if (type instanceof Class) {
            return getClassType((Class) type, clz);
        } else if (type instanceof ParameterizedType) {
            return getParameterizedType(clz, (ParameterizedType) type, contextClass);
        } else if (type instanceof GenericArrayType) {
            return getClassType(((GenericArrayType) type).getGenericComponentType(), clz, contextClass) + "[]";
        } else if (type instanceof TypeVariable) {
            if (contextClass != null) {
                return getClassType(TypeHelper.findActualClassFrom((TypeVariable) type, contextClass), clz, null);
            } else
                return ((TypeVariable) type).getName();
        } else {
            throw new RuntimeException("unknown type: " + type);
        }
    }

    private String getParameterizedType(TSClass clz, ParameterizedType pt, Class contextClass) {
        Class rawType = (Class) pt.getRawType();
        if (Collection.class.isAssignableFrom(rawType)) {
            return getClassType(pt.getActualTypeArguments()[0], clz, contextClass) + "[]";
        } else if (Map.class.isAssignableFrom(rawType)) {
            return String.format("Map<%s, %s>",
                    getClassType(pt.getActualTypeArguments()[0], clz, contextClass),
                    getClassType(pt.getActualTypeArguments()[1], clz, contextClass));
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(getClassType(rawType, clz))
                    .append("<");
            boolean isFirst = true;
            for (Type t : pt.getActualTypeArguments()) {
                if (!isFirst) builder.append(", ");
                builder.append(getClassType(t, clz, contextClass));
                isFirst = false;
            }
            builder.append(">");
            return builder.toString();
        }
    }

    private String getClassType(Class c, TSClass clz) {
        if (void.class.equals(c) || Void.class.equals(c)) {
            return "void";
        } else if (boolean.class.equals(c) || Boolean.class.equals(c)) {
            return "boolean";
        } else if (Common.inArray(c, NUMBERS) || Number.class.isAssignableFrom(c)) {
            return "number";
        } else if (char.class.equals(c) || Character.class.equals(c) || CharSequence.class.isAssignableFrom(c)) {
            return "string";
        } else if (Collection.class.isAssignableFrom(c)) {
            return "any[]";
        } else if (Map.class.isAssignableFrom(c)) {
            return "Map<any, any>";
        } else {
            clz.getImports().add(c);
            return getTSPojo(c).getClassName();
        }
    }

    private TSPojo getTSPojo(Class c) {
        // 处理过的不管
        Map<Class, TSClass> map = getTSClassMap(c);
        if (map.containsKey(c))
            return (TSPojo) map.get(c);

        TSPojo pojo = new TSPojo(c);
        map.put(c, pojo);

        if (!Object.class.equals(c.getGenericSuperclass()))
            pojo.setSuperClass(getClassType(c.getGenericSuperclass(), pojo, null));

        for (Field field : c.getDeclaredFields()) {
            int mod = field.getModifiers();
            if (!Modifier.isStatic(mod) && !Modifier.isTransient(mod)) {
                TSField tsField = new TSField();
                tsField.setName(field.getName());
                tsField.setType(getClassType(field.getGenericType(), pojo, null));
                pojo.getFields().add(tsField);
            }
        }

        while (!Object.class.equals(c.getSuperclass())) {
            c = c.getSuperclass();
            getTSPojo(c);
        }
        return pojo;
    }


    @Override
    protected String getTemplatePath() {
        return RESOURCE_PACKAGE;
    }

    @Override
    protected String getRenderName() {
        return RENDER_NAME;
    }
}
