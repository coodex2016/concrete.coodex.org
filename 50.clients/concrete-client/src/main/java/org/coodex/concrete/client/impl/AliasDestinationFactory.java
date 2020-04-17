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

import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.AbstractDestinationFactory;
import org.coodex.concrete.client.Destination;
import org.coodex.util.Common;

import java.util.Stack;

public class AliasDestinationFactory extends AbstractDestinationFactory<Destination> {

    public static final String ALIAS_PROTOCOLS = "alias:";
    private static final ThreadLocal<Stack<String>> stackThreadLocal = new ThreadLocal<>();

    @Override
    public Destination build(String module) {
        Stack<String> stack = stackThreadLocal.get();
        if (stack == null) {
            stack = new Stack<>();
            stackThreadLocal.set(stack);
        }
        if (stack.contains(module)) {
            StringBuilder builder = new StringBuilder();
            builder.append("client alias cycle reference: ")
                    .append(module).append(" -> ");
            for (int i = stack.size() - 1; i >= 0; i--) {
                String ref = stack.get(i);
                builder.append(ref);

                if (Common.isSameStr(ref, module)) break;

                builder.append(" -> ");
            }
            builder.append(".");

            throw new RuntimeException(builder.toString());
        }
        stack.push(module);
        try {
            return ClientHelper.getDestination(
                    getLocation(module).substring(ALIAS_PROTOCOLS.length()).trim());
        } finally {
            stack.pop();
        }
    }

    @Override
    public boolean accept(String module) {
        String location = getLocation(module);
        return !Common.isBlank(location) && location.toLowerCase().startsWith(ALIAS_PROTOCOLS);
    }
}
