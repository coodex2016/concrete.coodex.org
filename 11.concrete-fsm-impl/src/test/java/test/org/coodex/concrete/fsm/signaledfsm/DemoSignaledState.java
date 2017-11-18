package test.org.coodex.concrete.fsm.signaledfsm;

import org.coodex.concrete.fsm.SignaledState;

public class DemoSignaledState implements SignaledState {
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public long getSignal() {
        return value;
    }
}
