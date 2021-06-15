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
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.util.Common;

import javax.ws.rs.PathParam;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.common.ConcreteHelper.isPrimitive;
import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.IS_JAVA_9_AND_LAST;
import static org.coodex.concrete.support.jaxrs.javassist.CGContext.CLASS_POOL;

//import static org.coodex.concrete.jaxrs.ClassGenerator.FRONTEND_DEV_MODE;
//import static org.coodex.concrete.common.ConcreteHelper.isDevModel;
//import static org.coodex.concrete.common.ConcreteHelper.isDevModel;


/**
 * Jaxrs的方法构件对象
 * Created by davidoff shen on 2016-11-27.
 */
@SuppressWarnings("unused")
public abstract class AbstractMethodGenerator {

//    private final static Logger log = LoggerFactory.getLogger(AbstractMethodGenerator.class);

    private static final AtomicInteger REF = new AtomicInteger(0);
    private static final Class<?>[] PRIMITIVE_CLASSES = new Class<?>[]{
            boolean.class, byte.class, char.class, short.class, int.class,
            long.class, float.class, double.class
    };
    private static final Class<?>[] PRIMITIVE_BOX_CLASSES = new Class<?>[]{
            Boolean.class, Byte.class, Character.class, Short.class, Integer.class,
            Long.class, Float.class, Double.class
    };
    final private CGContext context;
    final private JaxrsUnit unit;


    public AbstractMethodGenerator(CGContext context, JaxrsUnit unit) {
        this.context = context;
        this.unit = unit;
    }

    protected CGContext getContext() {
        return context;
    }

//    protected final CtClass[] getParameterTypesForDemo(Class pojoClass) {
//        return getParameterTypesWith(pojoClass);
//    }

    public JaxrsUnit getUnit() {
        return unit;
    }

    /**
     * @return 获取javassist的参数类型
     */
    protected abstract CtClass[] getParameterTypes(Class<?> pojoClass);

    protected final CtClass[] getParameterTypesWith(Class<?> pojoClass, CtClass... addingCtClass) {
        int additionParamCount = addingCtClass == null ? 0 : addingCtClass.length;
        JaxrsParam[] params = getUnit().getParameters();
        JaxrsParam[] pojoParam = getUnit().getPojo();

        CtClass[] parameters = new CtClass[actualParamCount() + additionParamCount];
        boolean pojoAdded = false;
        for (int i = 0, index = 0; i < params.length; i++) {
            if (pojoClass != null && Common.inArray(params[i], pojoParam)) {
                if (pojoAdded) {
                    continue;
                }
                parameters[index + additionParamCount] = JavassistHelper.getCtClass(pojoClass, CLASS_POOL);
//                        CLASS_POOL
//                        .getOrNull(pojoClass.getName());
                pojoAdded = true;
            } else {
                parameters[index + additionParamCount] =
                        JavassistHelper.getCtClass(params[i].getType(), CLASS_POOL);
//                        CLASS_POOL
//                        .getOrNull(JavassistHelper.getTypeName(params[i].getType()));
            }
            index++;
        }
        if (additionParamCount > 0) {
            System.arraycopy(addingCtClass, 0, parameters, 0, addingCtClass.length);
        }
        return parameters;
    }

    /**
     * @return 获取参数签名类型
     */
    protected abstract SignatureAttribute.Type[] getSignatureTypes(Class<?> pojoClass);

    protected int actualParamCount() {
        return getUnit().getParameters().length -
                (getUnit().getPojoCount() == 0 ? 0 : (getUnit().getPojoCount() - 1));
    }

    private Class<?> pojoClass() throws CannotCompileException {
        if (getUnit().getPojoCount() > 1) {
            return createPojoClass(
                    getUnit().getPojo(),
                    String.format("%s.POJO$%s$%s$%08X",
                            getContext().getServiceClass().getPackage().getName(),
                            getContext().getServiceClass().getSimpleName(),
                            getUnit().getMethod().getName(),
                            REF.incrementAndGet())
            );
        } else {
            return null;
        }
    }

//    protected final SignatureAttribute.Type[] getSignatureTypesForDemo(Class pojoClass) {
//        return getSignatureTypesWith(pojoClass);
//    }

    protected final SignatureAttribute.Type[] getSignatureTypesWith(Class<?> pojoClass, SignatureAttribute.Type... addingTypes) {
        int additionParamCount = addingTypes == null ? 0 : addingTypes.length;
        JaxrsParam[] params = getUnit().getParameters();
        JaxrsParam[] pojoParam = getUnit().getPojo();

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
            System.arraycopy(addingTypes, 0, parameters, 0, addingTypes.length);
        }
        return parameters;
    }

//    protected final String getMethodBodyForDemo() {
//        return "{return ($r)mockResult(\"" + getUnit().getFunctionName() + "\");}";
//    }

    private Class<?> createPojoClass(JaxrsParam[] pojoParams, String className) throws CannotCompileException {
        CtClass ctClass = CLASS_POOL.makeClass(className);

        for (JaxrsParam param : pojoParams) {
            CtField field = new CtField(
//                    CLASS_POOL.getOrNull(param.getType().getName()),
                    JavassistHelper.getCtClass(param.getType(), CLASS_POOL),
                    param.getName(), ctClass);
            field.setModifiers(javassist.Modifier.PUBLIC);
            field.setGenericSignature(
                    JavassistHelper.getSignature(
                            JavassistHelper.classType(param.getGenericType(), getContext().getServiceClass())
                    )
            );
            ctClass.addField(field);
        }
//        log.debug("generate class: {} use neighbor {}", className, getContext().getServiceClass().getName());
        return IS_JAVA_9_AND_LAST.get() ?
                ctClass.toClass(getContext().getServiceClass()) :
                ctClass.toClass();
    }

//    protected final SignatureAttribute.Type getReturnSignatureTypeForDemo() {
//        return JavassistHelper.classType(unit.getGenericReturnType(), context.getServiceClass());
//    }

    /**
     * @return 获取方法源码
     */
    protected abstract String getMethodBody(Class<?> pojoClass);

//    protected CtClass getReturnTypeForDemo() {
//        return CLASS_POOL.getOrNull(unit.getReturnType().getName());
//    }

    /**
     * @return 方法返回类型签名
     */
    protected abstract SignatureAttribute.Type getReturnSignatureType();

    /**
     * @return 方法返回类型
     */
    protected abstract CtClass getReturnType();

    /**
     * @return 方法参数注解
     */
    protected abstract AttributeInfo getParameterAnnotationsAttribute();

    protected AttributeInfo getParameterAnnotationsAttributeWith(Annotation... adding) {
        int additionParamCount = adding == null ? 0 : adding.length;

        ParameterAnnotationsAttribute attributeInfo = new ParameterAnnotationsAttribute(
                getContext().getConstPool(), ParameterAnnotationsAttribute.visibleTag);

        JaxrsParam[] params = getUnit().getParameters();
        JaxrsParam[] pojoParams = getUnit().getPojo();

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
//        boolean isDevModel = isDevModel("jaxrs");
        Class<?> pojoClass = pojoClass();
        // 方法名、参数及签名
        CtMethod spiMethod = new CtMethod(
                /*isDevModel ? getReturnTypeForDemo() : */getReturnType(),
                methodName,
                /*isDevModel ? getParameterTypesForDemo(pojoClass) : */getParameterTypes(pojoClass),
                context.getNewClass());

        AttributeInfo attribute = /*isDevModel
                ? getParameterAnnotationsAttributeForDemo()
                : */getParameterAnnotationsAttribute();

        if (attribute != null)
            spiMethod.getMethodInfo().addAttribute(attribute);

        spiMethod.setModifiers(Modifier.PUBLIC);

        spiMethod.setGenericSignature(
                new SignatureAttribute.MethodSignature(
                        null,
                        /*isDevModel ? getSignatureTypesForDemo(pojoClass) : */getSignatureTypes(pojoClass),
                        /*isDevModel ? getReturnSignatureTypeForDemo() : */getReturnSignatureType(),
                        null).encode()
        );


        // body
        spiMethod.setBody(/*isDevModel ? getMethodBodyForDemo() :*/ getMethodBody(pojoClass));

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

    protected abstract String[] getContentType();

    private String box(Class<?> c, int index) {
        return box(c, "$" + index);
    }

    private String box(Class<?> c, String param) {
        int i = Common.indexOf(PRIMITIVE_CLASSES, c);
        if (i >= 0)
            return PRIMITIVE_BOX_CLASSES[i].getName() + ".valueOf(" + param + ")";
        else
            return param;
    }


    protected String getParamListSrc(Class<?> pojoClass, int startIndex) {
        StringBuilder buffer = new StringBuilder();
        JaxrsParam[] params = unit.getParameters();
        JaxrsParam[] pojoParams = unit.getPojo();
        int pojoIndex = -1;
        for (int i = 0, index = 0; i < params.length; i++) {
            if (i > 0) buffer.append(", ");
            if (pojoClass != null && Common.inArray(params[i], pojoParams)) {
                boolean first = false;
                if (pojoIndex < 0) {
                    first = true;
                    pojoIndex = i;
                }
                buffer.append(box(params[i].getType(), "$" + (pojoIndex + startIndex) + "." + params[i].getName()));

                if (!first) continue;
//                continue;
            } else {
                buffer.append(box(params[i].getType(), index + startIndex));
            }
            index++;
        }
        return buffer.toString();
    }


    protected String getPathParam(JaxrsParam parameter) {
//        PathParam pathParam = parameter.getDeclaredAnnotation(PathParam.class);
//        if (pathParam != null) return pathParam.value();
        PathParam pathParam1 = parameter.getDeclaredAnnotation(PathParam.class);
        return pathParam1 == null ?
                /* ((CGContext.isPrimitive(parameter.getType()) && !JaxRSHelper.isBigString(parameter)) ?
                        parameter.getName() : null) */
                ((isPrimitive(parameter.getType()) && !JaxRSHelper.postPrimitive(parameter)) ? parameter.getName() : null) :
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
