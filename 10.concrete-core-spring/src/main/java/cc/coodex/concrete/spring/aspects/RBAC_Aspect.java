package cc.coodex.concrete.spring.aspects;

import cc.coodex.concrete.common.RuntimeContext;
import cc.coodex.concrete.core.intercept.InterceptOrders;
import cc.coodex.concrete.core.intercept.atoms.RBAC;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;

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
