package cc.coodex.concrete.core.intercept;

import cc.coodex.concrete.common.RuntimeContext;
import cc.coodex.concrete.core.intercept.atoms.BeanValidation;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class BeanValidationInterceptor extends AbstractInterceptor {
    @Override
    public int getOrder() {
        return InterceptOrders.BEAN_VALIDATION;
    }

    @Override
    protected boolean accept(RuntimeContext context) {
        return BeanValidation.accept(context);
    }

    @Override
    protected void before(RuntimeContext context, MethodInvocation joinPoint) {
        BeanValidation.before(context, joinPoint);
    }
}
