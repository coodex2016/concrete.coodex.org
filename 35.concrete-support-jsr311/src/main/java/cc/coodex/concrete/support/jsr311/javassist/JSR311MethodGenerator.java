package cc.coodex.concrete.support.jsr311.javassist;

import cc.coodex.concrete.jaxrs.struct.Param;
import cc.coodex.concrete.jaxrs.struct.Unit;
import cc.coodex.concrete.support.jaxrs.javassist.AbstractMethodGenerator;
import cc.coodex.concrete.support.jaxrs.javassist.CGContext;
import cc.coodex.concrete.support.jaxrs.javassist.JavassistHelper;
import javassist.CtClass;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;

import javax.ws.rs.core.Response;


/**
 * Created by davidoff shen on 2016-11-26.
 */
public class JSR311MethodGenerator extends AbstractMethodGenerator {

    public JSR311MethodGenerator(CGContext context, Unit unit) {
        super(context, unit);
    }


    @Override
    protected CtClass[] getParameterTypes() {
        // 增加String tokenId
        return getParameterTypesWith(CGContext.CLASS_POOL.getOrNull(String.class.getName()));
    }

    @Override
    protected SignatureAttribute.Type[] getSignatureTypes() {
        // 增加String tokenId
        return getSignatureTypesWith(JavassistHelper.classType(String.class, getContext().getServiceClass()));
    }


    @Override
    protected String getMethodBody() {
//        String paramListSrc = getParamListSrc(1);
        String paramListSrc = getParamListSrc(2);
        return "{return ($r)execute(\"" + getUnit().getFunctionName() + "\", $1"
                + (paramListSrc.length() == 0 ? ", null" : (", new java.lang.Object[]{" + paramListSrc + "}")) + ");}";
    }

    @Override
    protected SignatureAttribute.Type getReturnSignatureType() {
        return JavassistHelper.classType(Response.class, getContext().getServiceClass());
    }

    @Override
    protected CtClass getReturnType() {
        return CGContext.CLASS_POOL.getOrNull(Response.class.getName());
    }


    @Override
    protected AttributeInfo getParameterAnnotationsAttribute() {
        return getParameterAnnotationsAttributeWith(getContext().tokenCookieParam());
    }

}
