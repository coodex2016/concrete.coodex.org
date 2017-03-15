package org.coodex.concrete.jaxrs;

import org.coodex.concrete.jaxrs.struct.Module;

/**
 * Created by davidoff shen on 2016-11-26.
 */
public interface ClassGenerator {

    boolean FRONTEND_DEV_MODE =
            System.getProperty(ClassGenerator.class.getPackage().getName() + ".devMode") != null;

    boolean isAccept(String desc);

    String getImplPostfix();

    Class<?> getSuperClass();

    Class<?> generatesImplClass(Module module) throws Throwable;

}
