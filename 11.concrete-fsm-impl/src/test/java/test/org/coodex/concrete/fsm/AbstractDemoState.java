package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.State;

public class AbstractDemoState implements State{

    private int value = 0;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
