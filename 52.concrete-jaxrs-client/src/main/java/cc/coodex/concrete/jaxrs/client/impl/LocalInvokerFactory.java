package cc.coodex.concrete.jaxrs.client.impl;

import cc.coodex.concrete.common.BeanProviderFacade;
import cc.coodex.concrete.jaxrs.client.Invoker;
import cc.coodex.concrete.jaxrs.client.InvokerFactory;
import cc.coodex.concrete.jaxrs.struct.Unit;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class LocalInvokerFactory implements InvokerFactory {

    static class LocalInvoker implements Invoker {

        @Override
        public Object invoke(Unit unit, Object[] args) throws Throwable {
            return unit.getMethod().invoke(
                    BeanProviderFacade.getBeanProvider().getBean(
                            unit.getDeclaringModule().getInterfaceClass()),
                    args);
        }
    }


    @Override
    public boolean accept(String domain) {
        return "local".equalsIgnoreCase(domain);
    }

    @Override
    public Invoker getInvoker(String domain) {
        return new LocalInvoker();
    }
}
