package test.org.coodex.concrete.fsm.signaledfsm;

import org.coodex.concrete.fsm.SignaledState;
import test.org.coodex.concrete.fsm.AbstractDemoState;

public class DemoSignaledState extends AbstractDemoState implements SignaledState {

    @Override
    public long getSignal() {
        return getValue();
    }
}
