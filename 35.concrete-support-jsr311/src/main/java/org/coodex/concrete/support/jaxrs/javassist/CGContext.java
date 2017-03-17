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

package org.coodex.concrete.support.jaxrs.javassist;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import org.coodex.concrete.jaxrs.AbstractJAXRSResource;
import org.coodex.concrete.jaxrs.CreatedByConcrete;
import org.coodex.util.Common;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by davidoff shen on 2016-11-24.
 */
public class CGContext {

    protected final Class<?> serviceClass;
    protected final CtClass newClass;
    protected final ClassFile classFile;
    protected final ConstPool constPool;

    public static final ClassPool CLASS_POOL = ClassPool.getDefault();

    static {
        CLASS_POOL.insertClassPath(new ClassClassPath(AbstractJavassistClassGenerator.class));
    }

    private static final Class[] PRIMITIVE_CLASSESS = new Class[]{
            String.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Void.class,
            boolean.class,
            char.class,
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            void.class,
    };

    public static boolean isPrimitive(Class c) {
        return Common.inArray(c, PRIMITIVE_CLASSESS);
    }

    public CGContext(Class<?> serviceClass, Class<?> superClass, String newClassName) {
        super();
        this.serviceClass = serviceClass;
        this.newClass = CLASS_POOL.makeClass(newClassName, CLASS_POOL.getOrNull(superClass.getName()));
        classFile = newClass.getClassFile();
        constPool = getClassFile().getConstPool();
    }


    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public ConstPool getConstPool() {
        return constPool;
    }

    public ClassFile getClassFile() {
        return classFile;
    }

    public CtClass getNewClass() {
        return newClass;
    }


    public Annotation path(String path) {
        if (Common.isBlank(path)) return null;
        Annotation anno = new Annotation(Path.class.getName(), constPool);
        anno.addMemberValue("value",
                new StringMemberValue(path, constPool));
        return anno;
    }

    public Annotation pathParam(String value) {
        Annotation anno = new Annotation(PathParam.class.getName(), constPool);
        anno.addMemberValue("value",
                new StringMemberValue(value, constPool));
        return anno;
    }


    private static final String[] HTTP_METHODS =
            {HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE};
    private static final Class<?>[] JAXRS_METHOD_CLASS =
            {GET.class, PUT.class, POST.class, DELETE.class};

    public Annotation httpMethod(String httpMethod) {
//        int index = -1;
//        for (int i = 0; i < HTTP_METHODS.length; i++) {
//            if (HTTP_METHODS[i].equalsIgnoreCase(httpMethod)) {
//                index = i;
//                break;
//            }
//        }
        int index = Common.findInArray(httpMethod, HTTP_METHODS);
        if (index >= 0)
            return new Annotation(JAXRS_METHOD_CLASS[index].getName(), constPool);
        else
            throw new RuntimeException("nonsupport http method: " + httpMethod);
    }

    public Annotation consumes() {
        Annotation anno = new Annotation(Consumes.class.getName(), constPool);
        ArrayMemberValue mv = new ArrayMemberValue(constPool);
        mv.setValue(new StringMemberValue[]{new StringMemberValue(
                MediaType.APPLICATION_JSON, constPool)});
        anno.addMemberValue("value", mv);
        return anno;
    }

    public Annotation produces() {
        Annotation anno = new Annotation(Produces.class.getName(), constPool);
        ArrayMemberValue mv = new ArrayMemberValue(constPool);
        mv.setValue(new StringMemberValue[]{new StringMemberValue(
                MediaType.APPLICATION_JSON, constPool)});
        anno.addMemberValue("value", mv);
        return anno;
    }

    public Annotation createInfo() {
        return new Annotation(CreatedByConcrete.class.getName(), constPool);
    }

    public Annotation tokenCookieParam() {
        Annotation anno = new Annotation(CookieParam.class.getName(), constPool);
        anno.addMemberValue("value",
                new StringMemberValue(AbstractJAXRSResource.TOKEN_ID_IN_COOKIE, constPool));
        return anno;
    }


}