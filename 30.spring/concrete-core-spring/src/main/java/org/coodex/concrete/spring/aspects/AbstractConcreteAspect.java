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
import org.aspectj.lang.annotation.Around;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.AbstractInterceptor;
import org.coodex.concrete.core.intercept.AbstractSyncInterceptor;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.core.intercept.ConcreteSyncInterceptor;
import org.springframework.core.Ordered;

import static org.coodex.concrete.spring.aspects.AspectJHelper.ASPECT_POINT;
import static org.coodex.util.TypeHelper.solve;
import static org.coodex.util.TypeHelper.typeToClass;

/**
 * Created by davidoff shen on 2016-09-01.
 */
@Deprecated
public abstract class AbstractConcreteAspect<T extends AbstractInterceptor> extends AbstractSyncInterceptor implements Ordered {


    private ConcreteInterceptor interceptor;

    @SuppressWarnings("unchecked")
    private synchronized ConcreteSyncInterceptor getInterceptor() {
        if (interceptor == null) {
            try {
                interceptor = (ConcreteInterceptor) (typeToClass(
                        solve(AbstractConcreteAspect.class.getTypeParameters()[0], this.getClass())))
                        .getConstructor(new Class[0]).newInstance();
            } catch (Throwable th) {
                throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
            }
        }
        return interceptor instanceof ConcreteSyncInterceptor ?
                (ConcreteSyncInterceptor) interceptor :
                asyncToSync(interceptor);
    }

    @Around(ASPECT_POINT)
    public final Object weaverPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        return invoke(AspectJHelper.proceedJoinPointToMethodInvocation(joinPoint));
    }


    @Override
    public final boolean accept(RuntimeContext context) {
        return getInterceptor().accept(context);
    }

    @Override
    public final Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
        return getInterceptor().around(context, joinPoint);
    }

    @Override
    public final void before(RuntimeContext context, MethodInvocation joinPoint) {
        getInterceptor().before(context, joinPoint);
    }

    @Override
    public final Object after(RuntimeContext context, MethodInvocation joinPoint, Object result) {
        return getInterceptor().after(context, joinPoint, result);
    }

    @Override
    public final Throwable onError(RuntimeContext context, MethodInvocation joinPoint, Throwable th) {
        return getInterceptor().onError(context, joinPoint, th);
    }

    @Override
    public final int getOrder() {
        return getInterceptor().getOrder();
    }
}
