package cc.coodex.concrete.spring.aspects;

import cc.coodex.concrete.common.RuntimeContext;
import cc.coodex.concrete.core.intercept.InterceptOrders;
import cc.coodex.concrete.core.intercept.atoms.ServiceTimingValidation;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;

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
    protected boolean accept(RuntimeContext context) {
        return super.accept(context) && ServiceTimingValidation.isTimingLimitService(context);
    }

    @Override
    protected void before(RuntimeContext context, MethodInvocation joinPoint) {
        ServiceTimingValidation.before(context, joinPoint);
    }
}
