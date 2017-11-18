package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.State;
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
