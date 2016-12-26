package cc.coodex.concrete.spring.aspects;

import cc.coodex.concrete.core.intercept.AbstractInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.core.Ordered;

/**
 * Created by davidoff shen on 2016-09-01.
 */
public abstract class AbstractConcreteAspect extends AbstractInterceptor implements Ordered {
    public static final String ASPECT_POINT = "target(cc.coodex.concrete.api.ConcreteService) && execution(public * *(..))";


    @Around(ASPECT_POINT)
    public Object weaverPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        return invoke(AspectJHelper.proceedJoinPointToMethodInvocation(joinPoint));
    }
    
}
