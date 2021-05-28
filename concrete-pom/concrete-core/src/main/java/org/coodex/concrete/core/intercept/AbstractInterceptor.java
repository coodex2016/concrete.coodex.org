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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.LocalServiceContext;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.intercept.annotations.*;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;

/**
 * Created by davidoff shen on 2016-09-07.
 */
public abstract class AbstractInterceptor implements ConcreteInterceptor {


    protected final static DefinitionContext getContext(MethodInvocation invocation) {
        if (invocation instanceof ConcreteMethodInvocation) {
            return ConcreteHelper.getDefinitionContext(((ConcreteMethodInvocation) invocation).getInterfaceClass(),
                    invocation.getMethod());
        } else {
            throw new IllegalArgumentException("need ConcreteMethodInvocation. " + invocation);
        }
    }


    protected final static boolean isServiceMethod(DefinitionContext context) {
        return ConcreteHelper.isConcreteService(context.getDeclaringMethod());
    }

    @Override
    public final boolean accept(DefinitionContext context) {
        return sideAccept() && accept_(context);
    }

    private boolean sideAccept() {
        ServiceContext serviceContext = getServiceContext();
        Class<? extends AbstractInterceptor> clz = this.getClass();
        if (serviceContext instanceof ServerSideContext) {
            return clz.getAnnotation(ServerSide.class) != null;
        } else if (serviceContext instanceof ClientSideContext) {
            return clz.getAnnotation(ClientSide.class) != null;
        } else if (serviceContext instanceof LocalServiceContext) {
            return clz.getAnnotation(Local.class) != null;
        } else if (serviceContext instanceof TestServiceContext) {
            return clz.getAnnotation(TestContext.class) != null;
        } else {
            return clz.getAnnotation(Default.class) != null || (
                    clz.getAnnotation(ServerSide.class) == null
                            && clz.getAnnotation(ClientSide.class) == null
                            && clz.getAnnotation(Default.class) == null);
        }
    }

    protected abstract boolean accept_(DefinitionContext context);

    //    private boolean isAtomLevel(){
//        return false;
//    }

//    @Override
//    public Object invoke(MethodInvocation invocation) throws Throwable {
////        Object lock = atom.get();
////
////        if(!isAtomLevel() && lock != null){
////            return invocation.proceed();
////        }
//
//        RuntimeContext context = getContext(invocation);
//        if (context == null || !accept(context)) {
//            return invocation.proceed();
//        }
//
//        before(context, invocation);
//        try {
//            Object result = around(context, invocation);
//            try {
//                after(context, invocation, result);
////            }catch(ConcreteException ce){
////                throw ce;
//            } catch (Throwable t) {
//                log.warn("Error occured in afterAdvice. {}", t.getLocalizedMessage(), t);
//            } finally {
//                return result;
//            }
//        } catch (ConcreteException ce) {
//            throw ce;
//        } catch (Throwable t) {
//            Throwable t2 = onError(context, invocation, t);
//            throw t2 == null ? t : t2;
//        }
//    }

//    @Override
//    public boolean accept(RuntimeContext context) {
//        return true;
//    }

//    @Override
//    private Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
//        return joinPoint.proceed();
//    }

    @Override
    public void before(DefinitionContext context, MethodInvocation joinPoint) {
    }

    @Override
    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
        return result;
    }

    @Override
    public Throwable onError(DefinitionContext context, MethodInvocation joinPoint, Throwable th) {
        return th;
    }


}
