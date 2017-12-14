/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.fsm.impl;

import org.coodex.closure.ClosureContext;
import org.coodex.closure.StackClosureContext;
import org.coodex.concrete.fsm.FSMContext;
import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.State;

public class FSMContextImpl implements FSMContext {

    public static class Context {
        private final State state;
        private final FiniteStateMachine machine;

        Context(State state, FiniteStateMachine machine) {
            this.state = state;
            this.machine = machine;
        }

        public State getState() {
            return state;
        }

        public FiniteStateMachine getMachine() {
            return machine;
        }
    }

    static ClosureContext<Context> closureContext = new StackClosureContext<Context>();

    @Override
    @SuppressWarnings("unchecked")
    public <S extends State> S getState() {
        return (S) closureContext.get().getState();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <FSM extends FiniteStateMachine> FSM getMachine() {
        return (FSM) closureContext.get().getMachine();
    }
}
