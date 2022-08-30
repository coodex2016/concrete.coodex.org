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

package org.coodex.concrete.apitools.jaxrs.service;

import org.coodex.concrete.apitools.AbstractRenderer;
import org.coodex.concrete.apitools.jaxrs.DocToolkit;
import org.coodex.concrete.apitools.jaxrs.EnumElementInfo;
import org.coodex.concrete.apitools.jaxrs.POJOPropertyInfo;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.mock.Mocker;
import org.coodex.util.Common;
import org.coodex.util.JSONSerializer;
import org.coodex.util.PojoInfo;
import org.coodex.util.PojoProperty;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.coodex.concrete.common.ConcreteHelper.isPrimitive;


/**
 * Created by davidoff shen on 2016-12-04.
 */
@SuppressWarnings("unused")
public class ServiceDocToolkit extends DocToolkit {
    private final Set<String> pojoTypes = new HashSet<>();
    private final String writeToPath;

    public ServiceDocToolkit(AbstractRenderer<?> render) {
        this(render, null);
    }

    public ServiceDocToolkit(AbstractRenderer<?> render, String writeToPath) {
        super(render);
        this.writeToPath = writeToPath;
    }

    @Override
    protected String getClassLabel(Class<?> clz) throws IOException {
        if (isPrimitive(clz) || clz.getPackage().getName().startsWith("java")) {
            return clz.getSimpleName();
        } else {
            buildPojo(clz);
            return "[" + clz.getSimpleName() + "](../pojos/" +
                    canonicalName(clz.getName()) +
                    ".md)";
        }
    }

    private void buildPojo(Class<?> clz) throws IOException {
        String name = canonicalName(clz.getName());
        if (!pojoTypes.contains(name)) {
            pojoTypes.add(name);
            if (clz.isEnum()) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", clz.getName());
                map.put("tool", this);
                map.put("elements", EnumElementInfo.of(Common.cast(clz)));
                getRender().writeTo(
                        (Common.isBlank(this.writeToPath) ? "" : (writeToPath + Common.FILE_SEPARATOR))
                                + "pojos/" + canonicalName(clz.getName()) + ".md", "enumerable.md", map);
            } else {
                List<POJOPropertyInfo> pojoPropertyInfos = new ArrayList<>();
                PojoInfo pojoInfo = new PojoInfo(clz);

                for (PojoProperty pojoProperty : pojoInfo.getProperties()) {
                    pojoPropertyInfos.add(new POJOPropertyInfo(pojoProperty));
                }

                Map<String, Object> map = new HashMap<>();
                map.put("properties", pojoPropertyInfos);
                map.put("type", clz.getName());
                map.put("tool", this);

                getRender().writeTo(
                        (Common.isBlank(this.writeToPath) ? "" : (writeToPath + Common.FILE_SEPARATOR))
                                + "pojos/" + canonicalName(clz.getName()) + ".md", "pojo.md", map);
            }
        }
    }

    public Set<String> getPojos() {
        return pojoTypes;
    }


    private String prevOfMock() {
        return Common.isBlank(writeToPath) ? "- **example result**:" : "";
    }

    public String mockResult(JaxrsUnit unit) {
        String result;
        if (void.class.equals(unit.getReturnType())) {
            result = "";

        } else if (String.class.equals(unit.getReturnType())) {
            result = prevOfMock() + "\n\n```\n" +
                    Mocker.mockMethod(unit.getMethod(), unit.getDeclaringModule().getInterfaceClass()) +
                    "\n```\n\n";
        } else {
            result = prevOfMock() + "\n```json\n" +
                    JSONSerializer.getInstance().toJson(Mocker.mockMethod(unit.getMethod(),
                            unit.getDeclaringModule().getInterfaceClass())) +
                    "\n```\n\n";
        }
        return result;
    }

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
