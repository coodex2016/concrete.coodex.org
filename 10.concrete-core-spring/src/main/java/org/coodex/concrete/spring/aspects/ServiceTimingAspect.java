package org.coodex.concrete.spring.aspects;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.InterceptOrders;
import org.coodex.concrete.core.intercept.atoms.ServiceTimingValidation;

/**
 * Created by davidoff shen on 2016-11-02.
 */
@Aspect
public class ServiceTimingAspect extends AbstractConcreteAspect {
    @Override
    public int getOrder() {
        return InterceptOrders.SERVICE_TIMING;
    }

    @Override
    public boolean accept(RuntimeContext context) {
        return super.accept(context) && ServiceTimingValidation.isTimingLimitService(context);
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        ServiceTimingValidation.before(context, joinPoint);
    }
}
