package cc.coodex.concrete.core.intercept;

import cc.coodex.concrete.common.ConcreteException;
import cc.coodex.concrete.common.RuntimeContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public abstract class AbstractInterceptor implements ConcreteInterceptor {

//    private final static ThreadLocal<Object> atom = new ThreadLocal<>();


    private final static Logger log = LoggerFactory.getLogger(AbstractInterceptor.class);


    protected final static RuntimeContext getContext(MethodInvocation joinPoint) {
        return RuntimeContext.getRuntimeContext(joinPoint.getMethod(),
                joinPoint.getThis().getClass());
    }

//    private boolean isAtomLevel(){
//        return false;
//    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
//        Object lock = atom.get();
//
//        if(!isAtomLevel() && lock != null){
//            return invocation.proceed();
//        }

        RuntimeContext context = getContext(invocation);
        if (context == null || !accept(context)) {
            return invocation.proceed();
        }

        before(context, invocation);
        try {
            Object result = around(context, invocation);
            try {
                after(context, invocation, result);
//            }catch(ConcreteException ce){
//                throw ce;
            } catch (Throwable t) {
                log.warn("Error occured in afterAdvice. {}", t.getLocalizedMessage(), t);
            } finally {
                return result;
            }
        } catch (ConcreteException ce) {
            throw ce;
        } catch (Throwable t) {
            Throwable t2 = onError(context, invocation, t);
            throw t2 == null ? t : t2;
        }
    }

    @Override
    public boolean accept(RuntimeContext context) {
        return true;
    }

    @Override
    public Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    @Override
    public void before(RuntimeContext context, MethodInvocation joinPoint) {
    }

    @Override
    public Object after(RuntimeContext context, MethodInvocation joinPoint, Object result) {
        return result;
    }

    @Override
    public Throwable onError(RuntimeContext context, MethodInvocation joinPoint, Throwable th) {
        return th;
    }
}
