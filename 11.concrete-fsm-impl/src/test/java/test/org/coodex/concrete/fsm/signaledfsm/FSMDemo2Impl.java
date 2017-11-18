package test.org.coodex.concrete.fsm.signaledfsm;

import org.coodex.concrete.fsm.SignaledState;
import org.coodex.concrete.fsm.WrongStateException;
import org.coodex.concrete.fsm.impl.AbstractFSM;

public class FSMDemo2Impl extends AbstractFSM<DemoSignaledState,FSMDemo2> implements FSMDemo2 {

    private void toX(int x) {
        DemoSignaledState state = getState();
        long oldValue = state.getSignal();
        state.setValue(x);
        System.out.println(String.format("from %d to %d[thread: %d]",oldValue, x, Thread.currentThread().getId()));
    }

    @Override
    public RuntimeException errorHandle(WrongStateException exception) {
        return exception;
    }

    @Override
    public void toZero() {
        toX(0);
    }

    @Override
    public void toOne() {
        toX(1);
    }

    @Override
    public void toTwo() {
        toX(2);
    }

    @Override
    public void toThree() {
        toX(3);
    }
}
