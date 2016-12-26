package cc.coodex.concrete.support.jsr311.javassist;


import cc.coodex.concrete.jaxrs.JaxRSModuleMaker;
import cc.coodex.concrete.jaxrs.struct.Unit;
import cc.coodex.concrete.support.jaxrs.javassist.AbstractJavassistClassGenerator;
import cc.coodex.concrete.support.jaxrs.javassist.AbstractMethodGenerator;
import cc.coodex.concrete.support.jaxrs.javassist.CGContext;
import cc.coodex.concrete.support.jsr311.AbstractJSR311Resource;

/**
 * Created by davidoff shen on 2016-11-24.
 */
public final class JSR311ClassGenerator extends AbstractJavassistClassGenerator {

    public static final String GENERATOR_NAME =
            JaxRSModuleMaker.JAX_RS_PREV + ".jsr311." + GENTERATOR_TOOLS_NAME + ".v1";


    @Override
    public boolean isAccept(String desc) {
        return GENERATOR_NAME.equalsIgnoreCase(desc);
    }

    @Override
    public String getImplPostfix() {
        return "$Jsr311Impl";
    }


    @Override
    public Class<?> getSuperClass() {
        return AbstractJSR311Resource.class;
    }

    @Override
    protected AbstractMethodGenerator getMethodGenerator(CGContext context, Unit unit) {
        return new JSR311MethodGenerator(context, unit);
    }
}
