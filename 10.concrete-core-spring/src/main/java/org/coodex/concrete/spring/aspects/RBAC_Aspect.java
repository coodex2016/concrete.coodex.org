package org.coodex.concrete.spring.aspects;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.InterceptOrders;
import org.coodex.concrete.core.intercept.atoms.RBAC;

/**
 * Created by davidoff shen on 2016-09-06.
 */
@Aspect
public class RBAC_Aspect extends AbstractConcreteAspect {

    @Override
    public boolean accept(RuntimeContext context) {
        return context != null;
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        RBAC.before(context, joinPoint);
    }

    @Override
    public int getOrder() {
        return InterceptOrders.RBAC;
    }
}
