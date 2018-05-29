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

package org.coodex.concrete.core.mocker;

import javassist.*;
import javassist.bytecode.SignatureAttribute;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.pojomocker.PojoBuilder;
import org.coodex.util.PojoInfo;
import org.coodex.util.PojoProperty;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by davidoff shen on 2017-05-15.
 */
public class JavassistPojoBuilder implements PojoBuilder {

    private static final AtomicLong ATOMIC_LONG = new AtomicLong(0);
    private final Map<String, Class> classCache = new HashMap<String, Class>();

    private static String upperFirstChar(String propertyName) {
        char[] chars = propertyName.toCharArray();
        if (chars.length > 0 && chars[0] <= 'z' && chars[0] >= 'a') {
            chars[0] = (char) (chars[0] + 'A' - 'a');
            return new String(chars);
        }
        return propertyName;
    }

    private Class getClass(PojoInfo pojoInfo) throws CannotCompileException, NotFoundException, ClassNotFoundException {
        String key = pojoInfo.getType().toString();
        synchronized (classCache) {
            if (!classCache.containsKey(key)) {
                classCache.put(key, buildClass(pojoInfo));
            }
        }
        return classCache.get(key);
    }

    private Class buildClass(PojoInfo pojoInfo) throws CannotCompileException, NotFoundException, ClassNotFoundException {
        ClassPool classPool = ClassPool.getDefault();
//        classPool.insertClassPath(new ClassClassPath(Class.forName(pojoInfo.getType().toString())));

        // 生成继承类
        CtClass newClass = classPool.makeClass(
                String.format("build.by.concrete.POJO$%08x", ATOMIC_LONG.getAndIncrement()),
                classPool.getOrNull(pojoInfo.getRowType().getName()));

        newClass.setSuperclass(classPool.get(pojoInfo.getRowType().getName()));

        newClass.setInterfaces(new CtClass[]{classPool.getOrNull(JavassistPojo.class.getName())});

        // 如果是ParameterizedType，定义泛型信息
        if (pojoInfo.getType() instanceof ParameterizedType) {
            newClass.setGenericSignature(
                    new SignatureAttribute.ClassSignature(null,
                            (SignatureAttribute.ClassType) JavassistHelper.classType(pojoInfo.getType(), null),
                            null).encode());
        }

        // java 5
        newClass.getClassFile().setVersionToJava5();

        // 默认构造方法
        CtConstructor ctConstructor = new CtConstructor(null, newClass);
        ctConstructor.setBody("{super();}");
        newClass.addConstructor(ctConstructor);

        // key方法
        newClass.addMethod(CtMethod.make("public java.lang.String __key(){ return \""
                + pojoInfo.getType().toString() + "\"; }", newClass));

        // 根据属性重载方法
        for (PojoProperty property : pojoInfo.getProperties()) {
            if (property.getMethod() != null) {
                String fieldGen = "__gen_by_concrete_" + property.getName();
                CtClass fieldType = classPool.getOrNull(getTypeName(property.getType()));
                newClass.addField(new CtField(fieldType, fieldGen, newClass));

                // get方法
                CtMethod method = new CtMethod(fieldType, property.getMethod().getName(), new CtClass[0], newClass);
                method.setBody("{return this." + fieldGen + "; }");
                newClass.addMethod(method);

                // set方法
                method = new CtMethod(
                        classPool.getOrNull("void"),
                        getSetterName(property.getName()), new CtClass[]{fieldType}, newClass);
                method.setBody("{this." + fieldGen + " = $1; }");
                newClass.addMethod(method);
            }
        }

        return newClass.toClass();
    }

    private String getSetterName(String propertyName) {
        return "set" + upperFirstChar(propertyName);
    }

    private String getGetterName(String propertyName) {
        return "get" + upperFirstChar(propertyName);
    }

    private String getTypeName(Type type) {
        if (type instanceof Class) {
            Class c = (Class) type;
            return c.isArray() ? getTypeName(c.getComponentType()) + "[]" : c.getName();
        } else if (type instanceof ParameterizedType) {
            return getTypeName(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return getTypeName(genericArrayType.getGenericComponentType()) + "[]";
        } else {
            throw new RuntimeException("unsupported type: " + type.toString());
        }
    }

    @Override
    public Object newInstance(PojoInfo pojoInfo) throws Throwable {
        return getClass(pojoInfo).newInstance();
    }

    @Override
    public void set(Object instance, PojoProperty property, Object value) throws Throwable {
        if (instance instanceof JavassistPojo) {
            Class c = classCache.get(((JavassistPojo) instance).__key());
            if (property.getMethod() != null) {
                @SuppressWarnings("unchecked")
                Method method = c.getMethod(getSetterName(property.getName()), new Class[]{MockerFacade.getComponentClass(property.getType())});
                method.invoke(instance, value);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object get(Object instance, PojoProperty property) throws Throwable {
        if (instance instanceof JavassistPojo) {
            Class c = classCache.get(((JavassistPojo) instance).__key());
            return c.getMethod(getGetterName(property.getName())).invoke(instance);
        }
        return null;
    }

    public interface JavassistPojo {
        String __key();
    }

}
