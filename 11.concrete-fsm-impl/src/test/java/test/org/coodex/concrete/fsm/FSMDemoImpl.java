package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.WrongStateException;
import org.coodex.concrete.fsm.impl.AbstractFSM;

public class FSMDemoImpl extends AbstractFSM<NumericState, FSMDemo> implements FSMDemo {

    private void toX(int x) {
        NumericState state = getState();
        int oldValue = state.getValue();
        state.setValue(x);
        System.out.println(String.format("from %d to %d[thread: %d]", oldValue, state.getValue(), Thread.currentThread().getId()));
    }

    @Override
    public RuntimeException errorHandle(WrongStateException exception) {
        return new DemoWrongStateException(getState(), getState().getValue());
    }

    @Override
    public void toZero() {
        toX(0);
        getSelf().toThree();
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
