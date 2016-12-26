package cc.coodex.concrete.core.intercept;

import cc.coodex.concrete.common.RuntimeContext;
import cc.coodex.concrete.core.intercept.atoms.ServiceTimingValidation;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by davidoff shen on 2016-11-02.
 */
public class ServiceTimingInterceptor extends AbstractInterceptor {


    @Override
    protected boolean accept(RuntimeContext context) {
        return super.accept(context) && ServiceTimingValidation.isTimingLimitService(context);
    }

    @Override
    public int getOrder() {
        return InterceptOrders.SERVICE_TIMING;
    }

    @Override
    protected void before(RuntimeContext context, MethodInvocation joinPoint) {
        ServiceTimingValidation.before(context, joinPoint);
    }
}
