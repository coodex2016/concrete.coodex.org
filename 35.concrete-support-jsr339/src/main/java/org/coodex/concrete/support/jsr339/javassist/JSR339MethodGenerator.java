package org.coodex.concrete.support.jsr339.javassist;

import javassist.CtClass;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.concrete.support.jaxrs.javassist.AbstractMethodGenerator;
import org.coodex.concrete.support.jaxrs.javassist.CGContext;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
 * Created by davidoff shen on 2016-11-27.
 */
public class JSR339MethodGenerator extends AbstractMethodGenerator {

    public JSR339MethodGenerator(CGContext context, Unit unit) {
        super(context, unit);
    }

    @Override
    protected CtClass[] getParameterTypes() {
        // 参数1：@Suspended AsyncResponse
        // 参数2: @CookieParam String tokenId
        return getParameterTypesWith(
                CGContext.CLASS_POOL.getOrNull(AsyncResponse.class.getName()),
                CGContext.CLASS_POOL.getOrNull(String.class.getName()));
//        Param[] params = getUnit().getParameters();
//        CtClass[] parameters = new CtClass[params.length + 1];
//
//        parameters[0] = CGContext.CLASS_POOL.getOrNull(AsyncResponse.class.getName());
//
//        for (int i = 0; i < params.length; i++) {
//            parameters[i + 1] = CGContext.CLASS_POOL
//                    .getOrNull(JavassistHelper.getTypeName(params[i].getType()));
//        }
//        return parameters;
    }

    @Override
    protected SignatureAttribute.Type[] getSignatureTypes() {
        // 参数1：@Suspended AsyncResponse
        // 参数2: @CookieParam String tokenId
        return getSignatureTypesWith(
                new SignatureAttribute.ClassType(AsyncResponse.class.getName()),
                new SignatureAttribute.ClassType(String.class.getName()));
//        Param[] params = getUnit().getParameters();
//        SignatureAttribute.Type[] parameters = new SignatureAttribute.Type[params.length + 1];
//
//        parameters[0] = new SignatureAttribute.ClassType(AsyncResponse.class.getName());
//
//        for (int i = 0; i < params.length; i++) {
//            parameters[i + 1] = JavassistHelper.classType(params[i].getGenericType(), getContext().getServiceClass());
//        }
//        return parameters;
    }

    @Override
    protected String getMethodBody() {
        String paramListSrc = getParamListSrc(3);
        return "{execute(\"" + getUnit().getFunctionName() + "\", $1, $2"
                + (paramListSrc.length() == 0 ? ", null" : (", new java.lang.Object[]{" + paramListSrc + "}")) + "); return null;}";
    }

    @Override
    protected SignatureAttribute.Type getReturnSignatureType() {
        return new SignatureAttribute.BaseType(void.class.getName());
    }

    @Override
    protected CtClass getReturnType() {
        return CGContext.CLASS_POOL.getOrNull(void.class.getName());
    }


    @Override
    protected AttributeInfo getParameterAnnotationsAttribute() {
        // 参数1：@Suspended AsyncResponse
        // 参数2: @CookieParam String tokenId
        return getParameterAnnotationsAttributeWith(
                new Annotation(Suspended.class.getName(), getContext().getConstPool()),
                getContext().tokenCookieParam()
        );
//        ParameterAnnotationsAttribute attributeInfo = new ParameterAnnotationsAttribute(
//                getContext().getConstPool(), ParameterAnnotationsAttribute.visibleTag);
//
//        Param[] params = getUnit().getParameters();
//        Annotation[][] annotations = new Annotation[params.length + 1][];
//
//        for (int i = 0; i < params.length; i++) {
//            String pathParamValue = getPathParam(params[i]);
//            if (pathParamValue != null) {
//                annotations[i + 1] = new Annotation[]{getContext().pathParam(pathParamValue)};
//            } else {
//                annotations[i + 1] = new Annotation[0];
//            }
//        }
//        annotations[0] = new Annotation[]{new Annotation(Suspended.class.getName(), getContext().getConstPool())};
//
//        attributeInfo.setAnnotations(annotations);
//        return attributeInfo;
    }
}
