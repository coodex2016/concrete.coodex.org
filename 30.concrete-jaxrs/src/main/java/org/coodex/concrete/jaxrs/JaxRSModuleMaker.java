package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.ModuleMaker;
import org.coodex.concrete.jaxrs.struct.Module;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class JaxRSModuleMaker implements ModuleMaker<Module> {

    public static final String JAX_RS_PREV = "JaxRS";

    @Override
    public boolean isAccept(String desc) {
        return desc != null
                && desc.length() > JAX_RS_PREV.length()
                && JAX_RS_PREV.equalsIgnoreCase(desc.substring(0, JAX_RS_PREV.length()));
    }

    @Override
    public Module make(Class<?> interfaceClass) {
        return new Module(interfaceClass);
    }
}
