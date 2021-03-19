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

package org.coodex.closure;

import java.util.Stack;
import java.util.function.Supplier;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public class StackClosureContext<T> extends AbstractClosureContext<Stack<T>> implements ClosureContext<T> {

    @Override
    public T get() {
        Stack<T> stack = getVariant();
        return stack == null ? null : stack.lastElement();
    }

    @Override
    public Object call(T var, Supplier<?> supplier) {
        if (supplier == null) return null;

        Stack<T> stack = getVariant();
        if (stack == null) {
            stack = new Stack<>();
            stack.push(var);
            try {
                return get(stack, supplier);
            } finally {
                stack.clear();
            }
        } else {
            stack.push(var);
            try {
                return supplier.get();
            } finally {
                stack.pop();
            }
        }
    }
}
