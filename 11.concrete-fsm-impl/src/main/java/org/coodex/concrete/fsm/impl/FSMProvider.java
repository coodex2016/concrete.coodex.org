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

import org.coodex.closure.Closure;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.fsm.*;
import org.coodex.util.ServiceLoader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class FSMProvider implements FiniteStateMachineProvider {

    private final static ServiceLoader<FiniteStateMachine> MACHINES = new ConcreteServiceLoader<FiniteStateMachine>() {
    };

    @Override
    public <S extends State, FSM extends FiniteStateMachine<? extends S>> FSM getMachine(S state, Class<? extends FSM> machineClass) {
        return (FSM) Proxy.newProxyInstance(
                machineClass.getClassLoader(),
                new Class[]{machineClass},
                new FSMInvocationHandle(state, MACHINES.getInstance(machineClass)));
    }
}

class FSMInvocationHandle implements InvocationHandler {
    private final State state;
    private final FiniteStateMachine original;

    private final static ServiceLoader<StateCondition> CONDITIONS = new ConcreteServiceLoader<StateCondition>() {
    };

    FSMInvocationHandle(State state, FiniteStateMachine original) {
        this.state = state;
        this.original = original;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {

        return FSMContextImpl.closureContext.run(state, new Closure() {
            @Override
            public Object run() {
                synchronized (state) {
                    StateTransfer transfer = method.getAnnotation(StateTransfer.class);
                    Class<? extends StateCondition> condition = transfer == null ? StateCondition.class : transfer.value();
                    if (condition != null && !StateCondition.class.equals(condition)) {
                        StateCondition sc = CONDITIONS.getInstance(condition);
                        if (sc != null && !sc.allow(state)) {
                            RuntimeException e = original.errorHandle(new WrongStateException(state));
                            if (e != null) throw e;
                        }
                    }
                    try {
                        return method.invoke(original, args);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

    }
}
