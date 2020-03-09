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

package org.coodex.concrete.apm;

import java.util.Stack;

public abstract class AbstractTrace implements Trace {

    private static ThreadLocal<Stack<AbstractTrace>> stackThreadLocal = new ThreadLocal<Stack<AbstractTrace>>();

    @Override
    public final Trace start() {
        return start(null);
    }

    protected abstract void actualStart(String name);

    private Stack<AbstractTrace> getStack() {
        if (stackThreadLocal.get() == null) {
            stackThreadLocal.set(new Stack<AbstractTrace>());
        }
        return stackThreadLocal.get();
    }

    private AbstractTrace current() {
        Stack<AbstractTrace> stack = getStack();
        return stack.isEmpty() ? null : stack.peek();
    }


    @Override
    public final Trace start(String name) {
//        AbstractTrace trace = current();
//        if (trace != null) {
//            this.appendTo(trace);
//        }
        getStack().push(this);
        this.actualStart(name);
        return this;
    }


    @Override
    public final void finish() {
        while (!getStack().isEmpty()) {
            AbstractTrace trace = current();
            if (trace != null && trace != this) {
                trace.finish();
            } else {
                getStack().pop();
                break;
            }
        }
        actualFinish();
    }

    protected abstract void actualFinish();
}
