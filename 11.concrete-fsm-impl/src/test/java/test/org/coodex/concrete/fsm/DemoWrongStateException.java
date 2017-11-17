package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.State;
import org.coodex.concrete.fsm.WrongStateException;

public class DemoWrongStateException extends WrongStateException {
    private final int oldState;

    public DemoWrongStateException(State state, int oldState) {
        super(state);
        this.oldState = oldState;
    }

    public int getOldState() {
        return oldState;
    }
}
