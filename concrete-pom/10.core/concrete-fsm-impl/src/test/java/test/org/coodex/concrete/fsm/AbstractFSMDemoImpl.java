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

package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.WrongStateException;
import org.coodex.concrete.fsm.impl.AbstractFSM;

public abstract class AbstractFSMDemoImpl<S extends AbstractDemoState, FSM extends FiniteStateMachine<S>> extends AbstractFSM<S, FSM> {

    private void toX(int x) {
        AbstractDemoState state = getState();
        int oldValue = state.getValue();
        state.setValue(x);
        System.out.println(String.format("from %d to %d[thread: %d]", oldValue, state.getValue(), Thread.currentThread().getId()));
    }

    public RuntimeException errorHandle(WrongStateException exception) {
        return new DemoWrongStateException(getState(), getState().getValue());
    }

    public void toZero() {
        toX(0);
    }

    public void toOne() {
        toX(1);
    }

    public void toTwo() {
        toX(2);
    }

    public void toThree() {
        toX(3);
    }
}
