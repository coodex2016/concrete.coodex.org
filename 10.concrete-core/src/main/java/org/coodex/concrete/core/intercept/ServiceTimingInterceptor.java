package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.atoms.ServiceTimingValidation;

/**
 * Created by davidoff shen on 2016-11-02.
 */
public class ServiceTimingInterceptor extends AbstractInterceptor {


    @Override
    public boolean accept(RuntimeContext context) {
        return super.accept(context) && ServiceTimingValidation.isTimingLimitService(context);
    }

    @Override
    public int getOrder() {
        return InterceptOrders.SERVICE_TIMING;
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        ServiceTimingValidation.before(context, joinPoint);
    }
}
