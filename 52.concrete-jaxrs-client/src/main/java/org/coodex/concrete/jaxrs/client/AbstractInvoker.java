package org.coodex.concrete.jaxrs.client;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.core.intercept.InterceptorChain;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.SPIFacade;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public abstract class AbstractInvoker implements Invoker {

    protected abstract MethodInvocation getInvocation(Unit unit, Object[] args, Object instance);

    @Override
    public final Object invoke(final Unit unit, final Object[] args, final Object instance) throws Throwable {
        return getInterceptorChain().invoke(getInvocation(unit, args, instance));
    }


    private static InterceptorChain interceptors;

    private static synchronized InterceptorChain getInterceptorChain() {
        if (interceptors == null) {
            SPIFacade<ConcreteInterceptor> spiFacade = new SPIFacade<ConcreteInterceptor>() {
            };
            interceptors = new InterceptorChain();
            for (ConcreteInterceptor interceptor : spiFacade.getAllInstances()) {
                interceptors.add(interceptor);
            }
        }
        return interceptors;
    }



}
