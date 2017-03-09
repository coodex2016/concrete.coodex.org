package cc.coodex.concrete.jaxrs.client;

import cc.coodex.concrete.common.DefinitionContext;
import cc.coodex.concrete.core.intercept.ConcreteInterceptor;
import cc.coodex.concrete.core.intercept.InterceptorChain;
import cc.coodex.concrete.jaxrs.JaxRSHelper;
import cc.coodex.concrete.jaxrs.struct.Module;
import cc.coodex.concrete.jaxrs.struct.Unit;
import cc.coodex.pojomocker.POJOMocker;
import cc.coodex.util.SPIFacade;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

import static cc.coodex.concrete.jaxrs.ClassGenerator.FRONTEND_DEV_MODE;

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
