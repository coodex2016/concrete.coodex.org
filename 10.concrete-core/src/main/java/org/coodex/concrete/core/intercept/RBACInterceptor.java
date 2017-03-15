package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.atoms.RBAC;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class RBACInterceptor extends AbstractInterceptor {
    @Override
    public int getOrder() {
        return InterceptOrders.RBAC;
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        RBAC.before(context, joinPoint);
    }
}
