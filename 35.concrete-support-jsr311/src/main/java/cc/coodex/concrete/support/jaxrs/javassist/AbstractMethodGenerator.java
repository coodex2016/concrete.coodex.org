package cc.coodex.concrete.support.jaxrs.javassist;

import cc.coodex.concrete.jaxrs.PathParam;
import cc.coodex.concrete.jaxrs.struct.Param;
import cc.coodex.concrete.jaxrs.struct.Unit;
import cc.coodex.util.Common;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;

import java.lang.reflect.Modifier;

import static cc.coodex.concrete.jaxrs.ClassGenerator.FRONTEND_DEV_MODE;


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
    protected abstract CtClass[] getParameterTypes();

    protected final CtClass[] getParameterTypesWith(CtClass... addingCtClass) {
        int count = addingCtClass == null ? 0 : addingCtClass.length;
        Param[] params = getUnit().getParameters();
        CtClass[] parameters = new CtClass[params.length + count];
        for (int i = 0; i < params.length; i++) {
            parameters[i + count] = CGContext.CLASS_POOL
                    .getOrNull(JavassistHelper.getTypeName(params[i].getType()));
        }
        if (count > 0) {
            for (int i = 0; i < addingCtClass.length; i++) {
                parameters[i] = addingCtClass[i];
            }
        }
        return parameters;
    }

    protected final CtClass[] getParameterTypesForDemo() {
        return getParameterTypesWith();
    }

    /**
     * @return 获取参数签名类型
     */
    protected abstract SignatureAttribute.Type[] getSignatureTypes();

    protected final SignatureAttribute.Type[] getSignatureTypesWith(SignatureAttribute.Type... addingTypes) {
        int count = addingTypes == null ? 0 : addingTypes.length;
        Param[] params = getUnit().getParameters();
        SignatureAttribute.Type[] parameters = new SignatureAttribute.Type[params.length + count];
        for (int i = 0; i < params.length; i++) {
            parameters[i + count] = JavassistHelper.classType(
                    params[i].getGenericType(), getContext().getServiceClass());
        }
        if (count > 0) {
            for (int i = 0; i < addingTypes.length; i++) {
                parameters[i] = addingTypes[i];
            }
        }
        return parameters;
    }

    protected final SignatureAttribute.Type[] getSignatureTypesForDemo() {
        return getSignatureTypesWith();
//        Param[] params = getUnit().getParameters();
//        SignatureAttribute.Type[] parameters = new SignatureAttribute.Type[params.length];
//        for (int i = 0; i < params.length; i++) {
//            parameters[i] = JavassistHelper.classType(
//                    params[i].getGenericType(), getContext().getServiceClass());
//        }
//        return parameters;
    }

    /**
     * @return 获取方法源码
     */
    protected abstract String getMethodBody();

    protected final String getMethodBodyForDemo() {
        return "{return ($r)mockResult();}";
    }

    /**
     * @return 方法返回类型签名
     */
    protected abstract SignatureAttribute.Type getReturnSignatureType();
//    {
//
//        return JavassistHelper.classType(unit.getGenericReturnType(), context.getServiceClass());
//    }

    protected final SignatureAttribute.Type getReturnSignatureTypeForDemo() {
        return JavassistHelper.classType(unit.getGenericReturnType(), context.getServiceClass());
    }

    /**
     * @return 方法返回类型
     */
    protected abstract CtClass getReturnType();

    protected CtClass getReturnTypeForDemo() {
        return CGContext.CLASS_POOL.getOrNull(unit.getReturnType().getName());
    }


    /**
     * @return 方法参数注解
     */
    protected abstract AttributeInfo getParameterAnnotationsAttribute();

    protected AttributeInfo getParameterAnnotationsAttributeWith(Annotation... adding) {
        int count = adding == null ? 0 : adding.length;

        ParameterAnnotationsAttribute attributeInfo = new ParameterAnnotationsAttribute(
                getContext().getConstPool(), ParameterAnnotationsAttribute.visibleTag);

        Param[] params = getUnit().getParameters();
        Annotation[][] annotations = new Annotation[params.length + count][];
        int added = 0;

        for (int i = 0; i < params.length; i++) {
            String pathParamValue = getPathParam(params[i]);
            if (pathParamValue != null) {
                annotations[i + count] = new Annotation[]{getContext().pathParam(pathParamValue)};
                added++;
            } else {
                annotations[i + count] = new Annotation[0];
            }
        }


        if (count > 0) {
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

    ;

    protected final AttributeInfo getParameterAnnotationsAttributeForDemo() {
        return getParameterAnnotationsAttributeWith();
//        ParameterAnnotationsAttribute attributeInfo = new ParameterAnnotationsAttribute(
//                getContext().getConstPool(), ParameterAnnotationsAttribute.visibleTag);
//
//        Param[] params = getUnit().getParameters();
//        Annotation[][] annotations = new Annotation[params.length][];
//        int added = 0;
//
//        for (int i = 0; i < params.length; i++) {
//            String pathParamValue = getPathParam(params[i]);
//            if (pathParamValue != null) {
//                annotations[i] = new Annotation[]{getContext().pathParam(pathParamValue)};
//                added++;
//            } else {
//                annotations[i] = new Annotation[0];
//            }
//        }
//
//        if (added == 0) return null;
//        attributeInfo.setAnnotations(annotations);
//        return attributeInfo;
    }


    final CtMethod generateMethod(String methodName) throws CannotCompileException {
        // 方法名、参数及签名
        CtMethod spiMethod = new CtMethod(
                FRONTEND_DEV_MODE ? getReturnTypeForDemo() : getReturnType(),
                methodName,
                FRONTEND_DEV_MODE ? getParameterTypesForDemo() : getParameterTypes(),
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
                        FRONTEND_DEV_MODE ? getSignatureTypesForDemo() : getSignatureTypes(),
                        FRONTEND_DEV_MODE ? getReturnSignatureTypeForDemo() : getReturnSignatureType(),
                        null).encode()
        );


        // body
        spiMethod.setBody(FRONTEND_DEV_MODE ? getMethodBodyForDemo() : getMethodBody());

        // 增加JSR311定义
        spiMethod.getMethodInfo().addAttribute(
                JavassistHelper.aggregate(context.getConstPool(),
                        context.path(path()),
                        httpMethod(),
                        context.consumes(),
                        context.produces(),
                        context.createInfo()));

        return spiMethod;
    }

    private static final Class<?>[] PRIMITIVE_CLASSES = new Class[]{
            boolean.class, byte.class, char.class, short.class, int.class,
            long.class, float.class, double.class
    };

    private static final Class<?>[] PRIMITIVE_BOX_CLASSES = new Class[]{
            Boolean.class, Byte.class, Character.class, Short.class, Integer.class,
            Long.class, Float.class, Double.class
    };


    private String box(Class<?> c, int index) {
        String param = "$" + index;
//        int i = -1;
//        for(int j = 0; j < PRIMITIVE_CLASSES.length; j ++){
//            if(c == PRIMITIVE_CLASSES[j]){
//                i = j;
//                break;
//            }
//        }
        int i = Common.findInArray(c, PRIMITIVE_CLASSES);
        if (i >= 0)
            return PRIMITIVE_BOX_CLASSES[i].getName() + ".valueOf(" + param + ")";
        else
            return param;
    }

    protected String getParamListSrc(int startIndex) {
        StringBuilder buffer = new StringBuilder();
        Param[] params = unit.getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) buffer.append(", ");
            buffer.append(box(params[i].getType(), i + startIndex));
        }
        return buffer.toString();
    }


    protected String getPathParam(Param parameter) {
        PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (pathParam != null) return pathParam.value();
        javax.ws.rs.PathParam pathParam1 = parameter.getAnnotation(javax.ws.rs.PathParam.class);
        return pathParam1 == null ?
                (CGContext.isPrimitive(parameter.getType()) ? parameter.getName() : null) :
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
