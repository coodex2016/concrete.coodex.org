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

/**
 * Created by davidoff shen on 2016-09-04.
 */
public class StackClosureContext<VariantType> extends AbstractClosureContext<Stack<VariantType>> implements ClosureContext<VariantType> {

    @Override
    public VariantType get() {
        Stack<VariantType> stack = $getVariant();
        return stack == null ? null : stack.lastElement();
    }

    @Override
    public VariantType get(VariantType defaultValue) {
        return get() == null ? defaultValue : get();
    }

    @Override
    @Deprecated
    public Object run(VariantType variant, Closure runnable) {
        if (runnable == null) return null;

        Stack<VariantType> stack = $getVariant();
        if (stack == null) {
            stack = new Stack<VariantType>();
            stack.push(variant);
            try {
                return closureRun(stack, runnable);
            } finally {
                stack.clear();
            }
        } else {
            stack.push(variant);
            try {
                return runnable.run();
            } finally {
                stack.pop();
            }
        }
    }

    @Override
    public Object call(VariantType var, CallableClosure callable) throws Throwable {
        if (callable == null) return null;

        Stack<VariantType> stack = $getVariant();
        if (stack == null) {
            stack = new Stack<VariantType>();
            stack.push(var);
            try {
                return closureRun(stack, callable);
            } finally {
                stack.clear();
            }
        } else {
            stack.push(var);
            try {
                return callable.call();
            } finally {
                stack.pop();
            }
        }
    }
}
