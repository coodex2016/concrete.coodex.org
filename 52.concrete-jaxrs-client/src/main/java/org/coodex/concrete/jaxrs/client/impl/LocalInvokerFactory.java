package org.coodex.concrete.jaxrs.client.impl;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.BeanProviderFacade;
import org.coodex.concrete.jaxrs.client.AbstractInvoker;
import org.coodex.concrete.jaxrs.client.ClientMethodInvocation;
import org.coodex.concrete.jaxrs.client.Invoker;
import org.coodex.concrete.jaxrs.client.InvokerFactory;
import org.coodex.concrete.jaxrs.struct.Unit;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class LocalInvokerFactory implements InvokerFactory {

    static class LocalInvoker extends AbstractInvoker {
        @Override
        protected MethodInvocation getInvocation(final Unit unit, final Object[] args, final Object instance) {
            return new ClientMethodInvocation(instance, unit, args) {
                @Override
                public Object proceed() throws Throwable {
                    return unit.getMethod().invoke(
                            BeanProviderFacade.getBeanProvider().getBean(
                                    unit.getDeclaringModule().getInterfaceClass()),
                            args);
                }
            };
        }
//            implements Invoker {
//
//        @Override
//        public Object invoke(Unit unit, Object[] args, Object instance) throws Throwable {
//            return unit.getMethod().invoke(
//                    BeanProviderFacade.getBeanProvider().getBean(
//                            unit.getDeclaringModule().getInterfaceClass()),
//                    args);
//        }
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
