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
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.message.Subscription;

import static org.coodex.concrete.core.intercept.InterceptOrders.OTHER;

@ServerSide
public abstract class AbstractTokenBaseTopicSubscribeInterceptor extends AbstractInterceptor {
    @Override
    protected boolean accept_(RuntimeContext context) {
//        ServiceContext serviceContext = getServiceContext();
//        return serviceContext != null && serviceContext instanceof ServerSideContext;
        return true;
    }

    /**
     * 返回<code>null</code>则表示不符合订阅条件，否则会加入到TokenLisenter中，在Token失效时会自动取消订阅
     *
     * @return
     */
    protected abstract Subscription subscribe();

    @Override
    public Object after(RuntimeContext context, MethodInvocation joinPoint, Object result) {
        TokenBaseTopicTokenEventListener.putSubscription(
                subscribe()
        );
        return super.after(context, joinPoint, result);
    }

    @Override
    public int getOrder() {
        return OTHER + 1;
    }


}
