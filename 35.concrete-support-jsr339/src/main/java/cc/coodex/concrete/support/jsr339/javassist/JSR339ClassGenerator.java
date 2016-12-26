package cc.coodex.concrete.support.jsr339.javassist;

import cc.coodex.concrete.jaxrs.JaxRSModuleMaker;
import cc.coodex.concrete.jaxrs.struct.Unit;
import cc.coodex.concrete.support.jaxrs.javassist.AbstractJavassistClassGenerator;
import cc.coodex.concrete.support.jaxrs.javassist.AbstractMethodGenerator;
import cc.coodex.concrete.support.jaxrs.javassist.CGContext;
import cc.coodex.concrete.support.jsr339.AbstractJSR339Resource;

/**
 * Created by davidoff shen on 2016-11-27.
 */
public class JSR339ClassGenerator extends AbstractJavassistClassGenerator {

    public static final String GENERATOR_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".jsr339." + GENTERATOR_TOOLS_NAME + ".v1";


    @Override
    public boolean isAccept(String desc) {
        return GENERATOR_NAME.equalsIgnoreCase(desc);
    }

    @Override
    public String getImplPostfix() {
        return "$Jsr339Impl";
    }

    @Override
    public Class<?> getSuperClass() {
        return AbstractJSR339Resource.class;
    }

    @Override
    protected AbstractMethodGenerator getMethodGenerator(CGContext context, Unit unit) {
        return new JSR339MethodGenerator(context, unit);
    }

}
