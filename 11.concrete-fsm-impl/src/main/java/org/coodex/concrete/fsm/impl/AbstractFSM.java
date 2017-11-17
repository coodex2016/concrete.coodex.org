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

import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.State;
import org.coodex.concrete.fsm.WrongStateException;

public abstract class AbstractFSM<S extends State, FSM extends FiniteStateMachine<S>> implements FiniteStateMachine<S> {

    protected S getState() {
        return (S) FSMContextImpl.closureContext.get().getState();
    }

    protected FSM getSelf() {
        return (FSM) FSMContextImpl.closureContext.get().getMachine();
    }


    @Override
    public RuntimeException errorHandle(WrongStateException exception) {
        return exception;
    }
}
