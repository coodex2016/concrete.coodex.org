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
import org.apache.commons.codec.binary.Base64;
import org.coodex.concrete.api.Signable;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.core.signature.SignUtil;

import java.util.Map;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;
import static org.coodex.concrete.core.signature.SignUtil.*;

@ServerSide
public class ServerSideSignatureInterceptor extends AbstractSyncInterceptor {




    @Override
    protected boolean accept_(DefinitionContext context) {
        return context.getAnnotation(Signable.class) != null;
    }

    @Override
    public void before(DefinitionContext context, MethodInvocation joinPoint) {
        serverSide_Verify(context, joinPoint, SignUtil.howToSign(context));
    }

    @Override
    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
        return super.after(context, joinPoint, result);
    }

    @Override
    public int getOrder() {
        return InterceptOrders.SIGNATURE;
    }
}
