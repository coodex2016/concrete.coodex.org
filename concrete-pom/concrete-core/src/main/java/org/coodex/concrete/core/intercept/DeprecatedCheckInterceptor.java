/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.Subjoin;
import org.coodex.concrete.common.SubjoinWrapper;
import org.coodex.concrete.common.WarningData;
import org.coodex.concrete.core.intercept.annotations.ServerSide;

import static org.coodex.concrete.common.ErrorCodes.WARNING_DEPRECATED;
import static org.coodex.concrete.core.intercept.InterceptOrders.DEPRECATED_CHECK;

@ServerSide
public class DeprecatedCheckInterceptor extends AbstractSyncInterceptor {

    private static ThreadLocal<Boolean> checking = new ThreadLocal<>();

    private Subjoin subjoin = SubjoinWrapper.getInstance();

    @Override
    protected boolean accept_(DefinitionContext context) {
        return context.getAnnotation(Deprecated.class) != null;
    }

    @Override
    public void before(DefinitionContext context, MethodInvocation joinPoint) {
        if (checking.get() == null) {
            checking.set(true);
            try {
                if (context.getDeclaringClass().getAnnotation(Deprecated.class) != null)
                    subjoin.putWarning(new WarningData(WARNING_DEPRECATED, context.getModuleName()));
                if (context.getDeclaringMethod().getAnnotation(Deprecated.class) != null)
                    subjoin.putWarning(new WarningData(WARNING_DEPRECATED, context.getDeclaringMethod().toGenericString()));
                super.before(context, joinPoint);
            } finally {
                checking.remove();
            }
        } else {
            super.before(context, joinPoint);
        }
    }

    @Override
    public int getOrder() {
        return DEPRECATED_CHECK;
    }
}
