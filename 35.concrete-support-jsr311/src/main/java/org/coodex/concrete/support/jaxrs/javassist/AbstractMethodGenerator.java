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

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.PathParam;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.Common;

import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.jaxrs.ClassGenerator.FRONTEND_DEV_MODE;
import static org.coodex.concrete.support.jaxrs.javassist.CGContext.CLASS_POOL;


/**
 * Jaxrs的方法构件对象
 * Created by davidoff shen on 2016-11-27.
 */
public abstract class AbstractMethodGenerator {


    final private CGContext context;
    final private Unit unit;


    public AbstractMethodGenerator(CGContext context, Unit unit) {
        this.context = context;
        this.unit = unit;
    }

    protected CGContext getContext() {
        return context;
    }

    public Unit getUnit() {
        return unit;
    }


    /**
     * @return 获取javassist的参数类型
     */
    protected abstract CtClass[] getParameterTypes(Class pojoClass);

    protected final CtClass[] getParameterTypesWith(Class pojoClass, CtClass... addingCtClass) {
        int additionParamCount = addingCtClass == null ? 0 : addingCtClass.length;
        Param[] params = getUnit().getParameters();
        Param[] pojoParam = getUnit().getPojo();

        CtClass[] parameters = new CtClass[actualParamCount() + additionParamCount];
        boolean pojoAdded = false;
        for (int i = 0, index = 0; i < params.length; i++) {
            if (pojoClass != null && Common.inArray(params[i], pojoParam)) {
                if (pojoAdded) continue;
                parameters[index + additionParamCount] = CLASS_POOL
                        .getOrNull(pojoClass.getName());
                pojoAdded = true;
            } else {
                parameters[index + additionParamCount] = CLASS_POOL
                        .getOrNull(JavassistHelper.getTypeName(params[i].getType()));
            }
            index++;
        }
        if (additionParamCount > 0) {
            for (int i = 0; i < addingCtClass.length; i++) {
                parameters[i] = addingCtClass[i];
            }
        }
        return parameters;
    }

    protected final CtClass[] getParameterTypesForDemo(Class pojoClass) {
        return getParameterTypesWith(pojoClass);
    }

    /**
     * @return 获取参数签名类型
     */
    protected abstract SignatureAttribute.Type[] getSignatureTypes(Class pojoClass);

    private static final AtomicInteger REF = new AtomicInteger(0);

    protected int actualParamCount() {
        return getUnit().getParameters().length -
                (getUnit().getPojoCount() == 0 ? 0 : (getUnit().getPojoCount() - 1));
    }

    private Class pojoClass() throws CannotCompileException {
        if (getUnit().getPojoCount() > 1)
            return createPojoClass(
                    getUnit().getPojo(),
                    String.format("POJO$%s$%s$%08X",
                            getContext().getServiceClass().getSimpleName(),
                            getUnit().getMethod().getName(),
                            REF.incrementAndGet())
            );
        else
            return null;
    }

    protected final SignatureAttribute.Type[] getSignatureTypesWith(Class pojoClass, SignatureAttribute.Type... addingTypes) {
        int additionParamCount = addingTypes == null ? 0 : addingTypes.length;
        Param[] params = getUnit().getParameters();
        Param[] pojoParam = getUnit().getPojo();

        SignatureAttribute.Type pojoType = pojoClass == null ? null : new SignatureAttribute.ClassType(pojoClass.getName());
        int paramCount = additionParamCount + actualParamCount();
        boolean pojoAdded = false;
        SignatureAttribute.Type[] parameters =
                new SignatureAttribute.Type[paramCount];
        for (int i = 0, index = 0; i < params.length; i++) {
            if (pojoType != null && Common.inArray(params[i], pojoParam)) {
                if (pojoAdded) continue;
                parameters[index + additionParamCount] = pojoType;
                pojoAdded = true;
            } else {
                parameters[index + additionParamCount] = JavassistHelper.classType(
                        params[i].getGenericType(), getContext().getServiceClass());
            }
            index++;
        }
        if (additionParamCount > 0) {
            for (int i = 0; i < addingTypes.length; i++) {
                parameters[i] = addingTypes[i];
            }
        }
        return parameters;
    }

    private Class createPojoClass(Param[] pojoParams, String className) throws CannotCompileException {
        CtClass ctClass = CLASS_POOL.makeClass(className);

        for (Param param : pojoParams) {
            CtField field = new CtField(CLASS_POOL.getOrNull(param.getType().getName()), param.getName(), ctClass);
            field.setModifiers(javassist.Modifier.PUBLIC);
            field.setGenericSignature(
                    JavassistHelper.getSignature(
                            JavassistHelper.classType(param.getGenericType(), getContext().getServiceClass())
                    )
            );
            ctClass.addField(field);
        }

        return ctClass.toClass();
    }

    protected final SignatureAttribute.Type[] getSignatureTypesForDemo(Class pojoClass) {
        return getSignatureTypesWith(pojoClass);
    }

    /**
     * @return 获取方法源码
     */
    protected abstract String getMethodBody(Class pojoClass);

    protected final String getMethodBodyForDemo() {
        return "{return ($r)mockResult();}";
    }

    /**
     * @return 方法返回类型签名
     */
    protected abstract SignatureAttribute.Type getReturnSignatureType();

    protected final SignatureAttribute.Type getReturnSignatureTypeForDemo() {
        return JavassistHelper.classType(unit.getGenericReturnType(), context.getServiceClass());
    }

    /**
     * @return 方法返回类型
     */
    protected abstract CtClass getReturnType();

    protected CtClass getReturnTypeForDemo() {
        return CLASS_POOL.getOrNull(unit.getReturnType().getName());
    }


    /**
     * @return 方法参数注解
     */
    protected abstract AttributeInfo getParameterAnnotationsAttribute();

    protected AttributeInfo getParameterAnnotationsAttributeWith(Annotation... adding) {
        int additionParamCount = adding == null ? 0 : adding.length;

        ParameterAnnotationsAttribute attributeInfo = new ParameterAnnotationsAttribute(
                getContext().getConstPool(), ParameterAnnotationsAttribute.visibleTag);

        Param[] params = getUnit().getParameters();
        Param[] pojoParams = getUnit().getPojo();

        int paramCount = additionParamCount + actualParamCount();
        Annotation[][] annotations = new Annotation[paramCount][];
        int added = 0;

        boolean pojoAdded = false;
        for (int i = 0, index = 0; i < params.length; i++) {
            if (Common.inArray(params[i], pojoParams)) {
                if (pojoAdded) continue;
                annotations[index + additionParamCount] = new Annotation[0];
                pojoAdded = true;
            } else {
                String pathParamValue = getPathParam(params[i]);
                if (pathParamValue != null) {
                    annotations[index + additionParamCount] = new Annotation[]{getContext().pathParam(pathParamValue)};
                    added++;
                } else {
                    annotations[index + additionParamCount] = new Annotation[0];
                }
            }
            index++;
        }


        if (additionParamCount > 0) {
            for (int i = 0; i < adding.length; i++) {
                if (adding[i] == null) {
                    annotations[i] = new Annotation[0];
                } else {
                    annotations[i] = new Annotation[]{adding[i]};
                    added++;
                }
            }
        }
        if (added == 0) return null;

        attributeInfo.setAnnotations(annotations);
        return attributeInfo;
    }

    protected final AttributeInfo getParameterAnnotationsAttributeForDemo() {
        return getParameterAnnotationsAttributeWith();
    }


    final CtMethod generateMethod(String methodName) throws CannotCompileException {
        Class pojoClass = pojoClass();
        // 方法名、参数及签名
        CtMethod spiMethod = new CtMethod(
                FRONTEND_DEV_MODE ? getReturnTypeForDemo() : getReturnType(),
                methodName,
                FRONTEND_DEV_MODE ? getParameterTypesForDemo(pojoClass) : getParameterTypes(pojoClass),
                context.getNewClass());

        AttributeInfo attribute = FRONTEND_DEV_MODE
                ? getParameterAnnotationsAttributeForDemo()
                : getParameterAnnotationsAttribute();

        if (attribute != null)
            spiMethod.getMethodInfo().addAttribute(attribute);

        spiMethod.setModifiers(Modifier.PUBLIC);

        spiMethod.setGenericSignature(
                new SignatureAttribute.MethodSignature(
                        null,
                        FRONTEND_DEV_MODE ? getSignatureTypesForDemo(pojoClass) : getSignatureTypes(pojoClass),
                        FRONTEND_DEV_MODE ? getReturnSignatureTypeForDemo() : getReturnSignatureType(),
                        null).encode()
        );


        // body
        spiMethod.setBody(FRONTEND_DEV_MODE ? getMethodBodyForDemo() : getMethodBody(pojoClass));

        // 增加JSR311定义
        spiMethod.getMethodInfo().addAttribute(
                JavassistHelper.aggregate(context.getConstPool(),
                        context.path(path()),
                        httpMethod(),
                        context.consumes(getContentType()),
                        context.produces(getContentType()),
                        context.createInfo(getUnit().getMethod().getParameterTypes())));

        return spiMethod;
    }

    protected abstract String [] getContentType();

    private static final Class<?>[] PRIMITIVE_CLASSES = new Class[]{
            boolean.class, byte.class, char.class, short.class, int.class,
            long.class, float.class, double.class
    };

    private static final Class<?>[] PRIMITIVE_BOX_CLASSES = new Class[]{
            Boolean.class, Byte.class, Character.class, Short.class, Integer.class,
            Long.class, Float.class, Double.class
    };


    private String box(Class<?> c, int index) {
        return box(c, "$" + index);
    }

    private String box(Class<?> c, String param) {
        int i = Common.findInArray(c, PRIMITIVE_CLASSES);
        if (i >= 0)
            return PRIMITIVE_BOX_CLASSES[i].getName() + ".valueOf(" + param + ")";
        else
            return param;
    }


    protected String getParamListSrc(Class pojoClass, int startIndex) {
        StringBuilder buffer = new StringBuilder();
        Param[] params = unit.getParameters();
        Param[] pojoParams = unit.getPojo();
        int pojoIndex = -1;
        for (int i = 0, index = 0; i < params.length; i++) {
            if (i > 0) buffer.append(", ");
            if (pojoClass != null && Common.inArray(params[i], pojoParams)) {
                if (pojoIndex < 0) {
                    pojoIndex = i;
                }
                buffer.append(box(params[i].getType(), "$" + (pojoIndex + startIndex) + "." + params[i].getName()));
                continue;
            }
            buffer.append(box(params[i].getType(), index + startIndex));
            index++;
        }
        return buffer.toString();
    }


    protected String getPathParam(Param parameter) {
        PathParam pathParam = parameter.getDeclaredAnnotation(PathParam.class);
        if (pathParam != null) return pathParam.value();
        javax.ws.rs.PathParam pathParam1 = parameter.getDeclaredAnnotation(javax.ws.rs.PathParam.class);
        return pathParam1 == null ?
                ((CGContext.isPrimitive(parameter.getType()) && !JaxRSHelper.isBigString(parameter)) ?
                        parameter.getName() : null) :
                pathParam1.value();
    }

    /**
     * @return javax.ws.rs.Path.value
     */
    private String path() {
        return getUnit().getName();
    }


    /**
     * javax.ws.rs.POST
     * javax.ws.rs.PUT
     * javax.ws.rs.GET
     * javax.ws.rs.DELETE
     *
     * @return HTTPMethod
     */
    private Annotation httpMethod() {
        return getContext().httpMethod(getUnit().getInvokeType());
    }


}
