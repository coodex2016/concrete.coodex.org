package org.coodex.concrete.common;

import org.coodex.util.Profile;
import org.coodex.util.SPIFacade;

/**
 * 使用concrete.properties解决冲突的SPIFacade。
 * <p>
 * 某个接口有多个services配置时，getInstance的时候会产生冲突，ConcreteSPIFacade使用concrete.properties解决冲突。
 * <pre>
 *     <i>interfaceClass</i>.provider = <i>service Class</i>
 * </pre>
 *
 * Created by davidoff shen on 2016-09-08.
 */
public abstract class ConcreteSPIFacade<T> extends SPIFacade<T> {


    private static Profile profile = ConcreteToolkit.getProfile();


    protected ConcreteSPIFacade() {
        super();
    }

    @Override
    protected T conflict() {
        String key = profile.getString(getInterfaceClass().getCanonicalName() + ".provider");
        return instances.containsKey(key) ? instances.get(key) : super.conflict();
    }

}

