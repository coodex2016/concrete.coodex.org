package org.coodex.concrete.spring.aspects;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public class AspectJHelper {
    protected final static Method getMethod(Signature sign) {
        return ((MethodSignature) sign).getMethod();
    }

    private static class ProceedJoinPointToMethodInvocation implements MethodInvocation {
        private ProceedingJoinPoint joinPoint;

        ProceedJoinPointToMethodInvocation(ProceedingJoinPoint joinPoint) {
            this.joinPoint = joinPoint;
        }

        @Override
        public Method getMethod() {
            return AspectJHelper.getMethod(joinPoint.getSignature());
        }

        @Override
        public Object[] getArguments() {
            return joinPoint.getArgs();
        }

        @Override
        public Object proceed() throws Throwable {
            return joinPoint.proceed();
        }

        @Override
        public Object getThis() {
            // 这里是否正确?
            return joinPoint.getThis();
        }

        @Override
        public AccessibleObject getStaticPart() {
            // 会不会有问题?
            return null;
        }
    }


    static MethodInvocation proceedJoinPointToMethodInvocation(ProceedingJoinPoint joinPoint){
        return new ProceedJoinPointToMethodInvocation(joinPoint);
    }


}
