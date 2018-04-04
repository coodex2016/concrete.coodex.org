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

package org.coodex.concrete.apitools;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.apitools.jaxrs.angular.meta.*;
import org.coodex.concrete.common.struct.AbstractModule;
import org.coodex.concrete.common.struct.AbstractParam;
import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.util.Common;
import org.coodex.util.TypeHelper;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public abstract class AbstractAngularRender<U extends AbstractUnit> extends AbstractRender {

    /// TODO: 处理方式不妥。如果Service method定义变换顺序后，会导致基于原版本的code全部出问题
    protected String getMethodName(String name, Set<String> methods) {
        String methodName = name;
        int prefix = 0;
        while (methods.contains(methodName)) {
            methodName = name + prefix++;
        }
        methods.add(methodName);
        return methodName;
    }

    protected abstract String getModuleType();

    protected static final ThreadLocal<Map<String, Map<Class, TSClass>>> CLASSES = new ThreadLocal<Map<String, Map<Class, TSClass>>>();

    private Map<String, Map<Class, TSClass>> getClasses() {
        return CLASSES.get();
    }

    protected String getModuleName(String moduleName) {
        return moduleName.charAt(0) == '@' ? moduleName : ("@" + moduleName);
    }

    private String getContextPath(String key) {
        StringBuilder builder = new StringBuilder();
        for (char ch : key.toCharArray()) {
            if (ch == '/') builder.append("../");
        }
        return builder.toString();
    }

    protected void packages(String contextPath) throws IOException {
        Map<String, Set<String>> services = new HashMap<String, Set<String>>();
        Set<String> providers = new HashSet<String>();
        Set<String> packages = new HashSet<String>();

        for (String key : getClasses().keySet()) {
            packages.add(key);
            Map<Class, TSClass> map = getClasses().get(key);
            Map<String, Object> toWrite = new HashMap<String, Object>();
            toWrite.put("contextPath", getContextPath(key));
            Set<Class> classSet = new HashSet<Class>();
            for (Class clz : map.keySet()) {
                if (ConcreteService.class.isAssignableFrom(clz)) {
                    toWrite.put("includeServices", Boolean.TRUE);
                    providers.add(map.get(clz).getClassName());
                    Set<String> set = services.containsKey(key) ? services.get(key) : new HashSet<String>();
                    set.add(map.get(clz).getClassName());
                    services.put(key, set);
                }
                classSet.addAll(map.get(clz).getImports());
            }

            Map<String, TSImport> imports = new HashMap<String, TSImport>();
            for (Class clz : classSet) {
                String packageName = getPackageKey(clz);
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
        Map<String, Object> toWrite = new HashMap<String, Object>();
        toWrite.put("services", services);
        toWrite.put("providers", providers);
        toWrite.put("packages", packages);
        toWrite.put("moduleType", getModuleType());
        writeTo(contextPath + "Concrete" + getModuleType() + "Module.ts", "concrete.ftl", toWrite);
    }

    private String getPackageKey(Class clz) {
        return clz.getPackage().getName().replace('.', '/');
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
        if (tsClass instanceof TSPojo) {
            return cache.get(((TSPojo) tsClass).getSuperType()) == null;
        }
        for (Class clz : tsClass.getImports()) {
            if (cache.get(clz) != null) return false;
        }
        return true;
    }


    protected void process(String moduleName, AbstractModule<U> module) {
        Class<?> clz = module.getInterfaceClass();
        Map<Class, TSClass> moduleMap = getTSClassMap(clz);
        if (moduleMap == null) return;

        TSModule tsModule = new TSModule(clz);
        tsModule.setBelong(moduleName);

        Set<String> methods = new HashSet<String>();
        for (U unit : module.getUnits()) {

            TSMethod method = new TSMethod();

            method.setName(getMethodName(unit.getMethod().getName(), methods));
            method.setHttpMethod(unit.getInvokeType());
            method.setReturnType(getClassType(unit.getGenericReturnType(), tsModule, clz));
            method.setMethodPath(getMethodPath(module, unit));

            method.setBody(getBody(unit));

            method.setParams(getParams(unit, tsModule));

            tsModule.getMethods().add(method);
        }
        moduleMap.put(clz, tsModule);
    }

    protected abstract String getMethodPath(AbstractModule<U> module, U unit);


    private List<TSParam> getParams(U unit, TSClass clz) {
        List<TSParam> fieldList = new ArrayList<TSParam>();
        for (int i = 0; i < unit.getParameters().length; i++) {
            AbstractParam param = unit.getParameters()[i];
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

    private String getClassType(Type type, TSClass clz, Class contextClass) {
        if (type instanceof Class) {
            Class c = (Class) type;
            if (c.isArray()) {
                return getClassType(c.getComponentType(), clz, contextClass) + "[]";
            } else
                return getClassType((Class) type, clz);
        } else if (type instanceof ParameterizedType) {
            return getParameterizedType(clz, (ParameterizedType) type, contextClass);
        } else if (type instanceof GenericArrayType) {
            return getClassType(((GenericArrayType) type).getGenericComponentType(), clz, contextClass) + "[]";
        } else if (type instanceof TypeVariable) {
            if (contextClass != null) {
                return getClassType(TypeHelper.solve((TypeVariable) type, contextClass), clz, null);
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

    private static final Class[] NUMBERS = new Class[]{
            byte.class, int.class, short.class, long.class, float.class, double.class
    };

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
        } else if (Object.class.equals(c)) {
            return "any";
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

        if (Object.class.equals(c)) {
            return pojo;
        }

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


    protected abstract String getBody(U unit);
}
