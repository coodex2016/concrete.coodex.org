package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.atoms.BeanValidation;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class BeanValidationInterceptor extends AbstractInterceptor {
    @Override
    public int getOrder() {
        return InterceptOrders.BEAN_VALIDATION;
    }

    @Override
    public boolean accept(RuntimeContext context) {
        return BeanValidation.accept(context);
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        BeanValidation.before(context, joinPoint);
    }
}
