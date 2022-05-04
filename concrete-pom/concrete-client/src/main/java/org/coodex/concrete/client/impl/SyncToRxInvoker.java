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

package org.coodex.concrete.client.impl;

import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.util.Common;

import java.util.concurrent.CompletableFuture;

import static org.coodex.concrete.ClientHelper.getRxClientScheduler;
import static org.coodex.util.Common.cast;

public class SyncToRxInvoker extends AbstractRxInvoker {
    private final AbstractSyncInvoker invoker;

    public SyncToRxInvoker(AbstractSyncInvoker invoker) {
        super(invoker.getDestination());
        this.invoker = invoker;
    }

    @Override
    protected CompletableFuture<?> futureInvoke(DefinitionContext runtimeContext, ServiceContext serviceContext, Object[] args) {
        final CompletableFuture<?> completableFuture = new CompletableFuture<>();
        getRxClientScheduler().execute(() -> {
            try {
                ConcreteContext.runWithContext(/*buildContext(runtimeContext)*/ serviceContext, () -> {
                    try {
                        completableFuture.complete(cast(invoker.execute(
                                runtimeContext.getDeclaringClass(),
                                runtimeContext.getDeclaringMethod(),
                                args
                        )));
                    } catch (Throwable throwable) {
                        throw Common.rte(throwable);
                    }
                });

            } catch (Throwable th) {
                completableFuture.completeExceptionally(th);
            }
        });
        return completableFuture;
    }

    @Override
    public ServiceContext buildContext(DefinitionContext context) {
        return invoker.buildContext(context);
    }
}
