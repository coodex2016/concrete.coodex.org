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

import org.apache.commons.beanutils.BeanUtils;
import org.coodex.closure.Closure;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.fsm.*;
import org.coodex.util.Common;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger log = LoggerFactory.getLogger(FSMProvider.class);

    private final State state;
    private final FiniteStateMachine original;

    private final static ServiceLoader<StateCondition> CONDITIONS = new ConcreteServiceLoader<StateCondition>() {
    };

    private final static ServiceLoader<IdentifiedStateContainer> CONTAINER_SERVICE_LOADER
            = new ConcreteServiceLoader<IdentifiedStateContainer>() {
    };


    FSMInvocationHandle(State state, FiniteStateMachine original) {
        this.state = state;
        this.original = original;
    }

    private Class<? extends StateCondition> getCondition(Method action) {
        Guard guard = action.getAnnotation(Guard.class);
        if (guard != null) return guard.value();
        StateTransfer transfer = action.getAnnotation(StateTransfer.class);
        return transfer == null ? null : transfer.value();
    }


    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        synchronized (state) {
            final FSMContextImpl.Context context = FSMContextImpl.closureContext.get();
            final State current = context == null ? state : context.getState();


            return FSMContextImpl.closureContext.run(new FSMContextImpl.Context(Common.deepCopy(current), (FiniteStateMachine) proxy), new Closure() {


                @Override
                public Object run() {
                    synchronized (state) {
                        try {
                            Class<? extends StateCondition> condition = getCondition(method);
                            boolean isSignaledState = current instanceof SignaledState;
                            SignaledGuard signaledGuard = method.getAnnotation(SignaledGuard.class);
                            if (condition != null && !StateCondition.class.equals(condition)) {
                                StateCondition sc = CONDITIONS.getInstance(condition);
                                if (sc != null && !sc.allow(current)) {
                                    RuntimeException e = original.errorHandle(new WrongStateException(current));
                                    if (e != null) throw e;
                                }
                            }

                            if (isSignaledState && signaledGuard != null && signaledGuard.allowed().length > 0) {
                                SignaledState signaledState = (SignaledState) current;
                                boolean allow = false;
                                long currentSignal = signaledState.getSignal();
                                for (long signal : signaledGuard.allowed()) {
                                    if (signal == currentSignal) {
                                        allow = true;
                                        break;
                                    }
                                }
                                if (!allow) {
                                    RuntimeException e = original.errorHandle(new WrongStateException(current));
                                    if (e != null) throw e;
                                }
                            }

                            try {
                                Object o = method.invoke(original, args);
                                BeanUtils.copyProperties(current, FSMContextImpl.closureContext.get().getState());
                                return o;
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            } catch (InvocationTargetException e) {
                                if (e.getTargetException() instanceof WrongStateException) {
                                    RuntimeException x = original.errorHandle((WrongStateException) e.getTargetException());
                                    if (x != null) throw x;
                                } else if (e.getTargetException() instanceof RuntimeException) {
                                    throw (RuntimeException) e.getTargetException();
                                }
                                throw new RuntimeException(e);
                            } finally {

                            }
                        }finally {
                            try {
                                if (context == null && state instanceof IdentifiedState) {
                                    IdentifiedState identifiedState = (IdentifiedState) state;
                                    CONTAINER_SERVICE_LOADER.getInstance().release(identifiedState.getId());
                                }
                            } catch (Throwable th) {
                                log.warn(th.getLocalizedMessage(), th);
                            }
                        }

                    }

                }
            });
        }

    }
}
