package cc.coodex.concrete.core.intercept;

import cc.coodex.concrete.common.RuntimeContext;
import cc.coodex.concrete.core.intercept.atoms.RBAC;
import org.aopalliance.intercept.MethodInvocation;

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
