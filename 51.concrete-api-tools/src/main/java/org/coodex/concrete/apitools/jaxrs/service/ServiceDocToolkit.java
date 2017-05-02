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

package org.coodex.concrete.apitools.jaxrs.service;

import org.coodex.concrete.apitools.jaxrs.AbstractRender;
import org.coodex.concrete.apitools.jaxrs.DocToolkit;
import org.coodex.concrete.apitools.jaxrs.POJOPropertyInfo;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.util.ReflectHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;


/**
 * Created by davidoff shen on 2016-12-04.
 */
public class ServiceDocToolkit extends DocToolkit {
    public ServiceDocToolkit(AbstractRender render) {
        super(render);
    }

    @Override
    protected String getClassLabel(Class<?> clz) throws IOException {
        if (JaxRSHelper.isPrimitive(clz) || clz.getPackage().getName().startsWith("java"))
            return clz.getSimpleName();
        else {
            buildPojo(clz);
            StringBuilder builder = new StringBuilder("[");
            builder.append(clz.getSimpleName()).append("](../pojos/")
                    .append(canonicalName(clz.getName()))
                    .append(".md)");
            return builder.toString();
        }
    }

    private void buildPojo(Class<?> clz) throws IOException {
        String name = canonicalName(clz.getName());
            if (!pojoTypes.contains(name)) {
            pojoTypes.add(name);

            List<POJOPropertyInfo> pojoPropertyInfos = new ArrayList<POJOPropertyInfo>();


            for (Method method : clz.getMethods()) {
                if (isProperty(method))
                    pojoPropertyInfos.add(new POJOPropertyInfo(clz, method));
            }

            for (Field field : ReflectHelper.getAllDeclaredFields(clz)) {
                if (isProperty(field))
                    pojoPropertyInfos.add(new POJOPropertyInfo(clz, field));
            }


//            pojoTypes.add(canonicalName(clz.getName()));
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("properties", pojoPropertyInfos);
            map.put("type", clz.getName());
            map.put("tool", this);

            getRender().writeTo("pojos/" + canonicalName(clz.getName()) + ".md", "pojo.md", map);
        }
    }

    private Set<String> pojoTypes = new HashSet<String>();

    public Set<String> getPojos() {
        return pojoTypes;
    }

//    @Override
//    protected String getTypeName(Class<?> clz, Class<?> contextClass) {
//        try {
//            return isPojo(clz) ? build(clz,contextClass) : clz.getSimpleName();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    private String build(Class<?> clz, Class<?> contextClass) throws IOException {
//        if (!pojoTypes.contains(clz)) {
//            List<POJOPropertyInfo> pojoPropertyInfos = new ArrayList<POJOPropertyInfo>();
//
//
//            for (Method method : clz.getMethods()) {
//                if (isProperty(method))
//                    pojoPropertyInfos.add(new POJOPropertyInfo(contextClass, method));
//            }
//
//            for (Field field : ReflectHelper.getAllDeclaredFields(clz)) {
//                if (isProperty(field))
//                    pojoPropertyInfos.add(new POJOPropertyInfo(contextClass, field));
//            }
//
//
//            pojoTypes.add(canonicalName(clz.getName()));
//            Map<String, Object> map = new HashMap<String, Object>();
//            map.put("properties", pojoPropertyInfos);
//            map.put("type", clz.getName());
//            map.put("tool", this);
//
//            getRender().writeTo("pojos/" + canonicalName(clz.getName()) + ".md", "pojo.md", map);
//        }
//        StringBuilder builder = new StringBuilder("[");
//        builder.append(clz.getSimpleName()).append("](../pojos/").append(canonicalName(clz.getName())).append(".md)");
//        return builder.toString();
//    }

    private boolean isProperty(Field field) {
        int mod = field.getModifiers();
        return Modifier.isPublic(mod)
                && !Modifier.isStatic(mod)
                && !Modifier.isTransient(mod);
    }

    private boolean isProperty(Method method) {
        String name = method.getName();
        return method.getDeclaringClass() != Object.class
                && Modifier.isPublic(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers())
                && !Modifier.isTransient(method.getModifiers())
                && (name.startsWith("get") || (name.startsWith("is") && method.getReturnType() == boolean.class));

    }


}
