package test.org.coodex.concrete.fsm.signaledfsm;

import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.SignaledGuard;
import org.coodex.concrete.fsm.SignaledState;

public interface FSMDemo2 extends FiniteStateMachine<DemoSignaledState> {

    @SignaledGuard(allowed = 3)
    void toZero();
    @SignaledGuard(allowed = 0)
    void toOne();
    @SignaledGuard(allowed = 1)
    void toTwo();
    @SignaledGuard(allowed = 2)
    void toThree();
}
