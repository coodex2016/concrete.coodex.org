package test.org.coodex.concrete.fsm.identified;

import org.coodex.concrete.fsm.IdentifiedState;
import org.coodex.concrete.fsm.SignaledState;
import test.org.coodex.concrete.fsm.AbstractDemoState;

public class DemoIdState extends AbstractDemoState implements IdentifiedState<String>, SignaledState {
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public long getSignal() {
        return getValue();
    }

}
