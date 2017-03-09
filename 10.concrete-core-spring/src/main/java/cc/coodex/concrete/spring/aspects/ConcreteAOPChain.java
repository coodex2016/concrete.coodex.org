package cc.coodex.concrete.spring.aspects;

import cc.coodex.concrete.common.ConcreteHelper;
import cc.coodex.concrete.core.intercept.ConcreteInterceptor;
import cc.coodex.concrete.core.intercept.InterceptorChain;
import cc.coodex.util.Profile;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * Created by davidoff shen on 2016-09-07.
 */
@Aspect
public class ConcreteAOPChain extends InterceptorChain implements Ordered {

    private static final Profile profile = ConcreteHelper.getProfile();

    protected static final String ASPECT_POINT = AbstractConcreteAspect.ASPECT_POINT;

    public ConcreteAOPChain(List<ConcreteInterceptor> interceptors) {
        super(interceptors);
    }

    @Override
    public int getOrder() {
        return profile.getInt(ConcreteAOPChain.class.getCanonicalName() + ".order", 0);
    }

    @Around(ASPECT_POINT)
    public Object weaverPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        return invoke(AspectJHelper.proceedJoinPointToMethodInvocation(joinPoint));
    }
}
