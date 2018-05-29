/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    static MethodInvocation proceedJoinPointToMethodInvocation(ProceedingJoinPoint joinPoint) {
        return new ProceedJoinPointToMethodInvocation(joinPoint);
    }

    private static class ProceedJoinPointToMethodInvocation implements MethodInvocation {
        private ProceedingJoinPoint joinPoint;
        private Method method = null;

        ProceedJoinPointToMethodInvocation(ProceedingJoinPoint joinPoint) {
            this.joinPoint = joinPoint;
        }

        @Override
        public synchronized Method getMethod() {
            if (method == null) {
                method = AspectJHelper.getMethod(joinPoint.getSignature());
                int paramCount = method.getParameterTypes().length;
                for (Method m : getThis().getClass().getMethods()) {
                    if (m.getName().equals(method.getName()) && m.getParameterTypes().length == paramCount) {
                        method = m;
                        break;
                    }
                }
            }
            return method;
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
            return joinPoint.getTarget();
        }

        @Override
        public AccessibleObject getStaticPart() {
            // 会不会有问题?
            return null;
        }
    }


}
