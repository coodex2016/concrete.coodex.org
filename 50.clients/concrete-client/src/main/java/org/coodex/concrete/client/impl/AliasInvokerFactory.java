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

package org.coodex.concrete.client.impl;

import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.Invoker;
import org.coodex.concrete.client.InvokerFactory;
import org.coodex.concrete.common.Assert;
import org.coodex.util.Common;

import java.util.Stack;

public class AliasInvokerFactory implements InvokerFactory {

    public static final String ALIAS_PROTOCOLS = "alias:";
    private static ThreadLocal<Stack<String>> stackThreadLocal = new ThreadLocal<Stack<String>>();

    @Override
    public Invoker getInvoker(Destination destination) {
        Stack<String> stack = stackThreadLocal.get();
        if (stack == null) {
            stack = new Stack<String>();
            stackThreadLocal.set(stack);
        }
        if (stack.contains(destination.getIdentify())) {
            StringBuilder builder = new StringBuilder();
            builder.append("client alias cycle reference: ").append(destination.getIdentify()).append(" -> ");
            for (int i = stack.size() - 1; i >= 0; i--) {
                String ref = stack.get(i);
                builder.append(ref);

                if (Common.isSameStr(ref, destination.getIdentify())) break;

                builder.append(" -> ");
            }
            builder.append(".");
            Assert.is(true, builder.toString());
        }
        stack.push(destination.getIdentify());
        try {
            Destination aliasTo = ClientHelper.getDestination(
                    destination.getLocation().substring(ALIAS_PROTOCOLS.length()).trim());
            return ClientHelper.getInvokerFactoryProviders()
                    .getServiceInstance(aliasTo)
                    .getInvoker(aliasTo);
        } finally {
            stack.pop();
        }
    }

    @Override
    public boolean accept(Destination param) {
        return !Common.isBlank(param.getLocation()) && param.getLocation().toLowerCase().startsWith(ALIAS_PROTOCOLS);
    }
}
