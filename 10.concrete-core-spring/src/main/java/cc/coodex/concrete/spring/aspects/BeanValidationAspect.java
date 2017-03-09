package cc.coodex.concrete.spring.aspects;

import cc.coodex.concrete.common.RuntimeContext;
import cc.coodex.concrete.core.intercept.InterceptOrders;
import cc.coodex.concrete.core.intercept.atoms.BeanValidation;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.annotation.Aspect;

/**
 * Created by davidoff shen on 2016-09-05.
 */
@Aspect
public class BeanValidationAspect extends AbstractConcreteAspect {

    @Override
    public boolean accept(RuntimeContext context) {
        return BeanValidation.accept(context);
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
        BeanValidation.before(context, joinPoint);
    }

    @Override
    public int getOrder() {
        return InterceptOrders.BEAN_VALIDATION;
    }
}
