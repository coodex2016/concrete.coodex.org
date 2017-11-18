package test.org.coodex.concrete.fsm.identified;

import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.SignaledGuard;

public interface DemoIdFSM extends FiniteStateMachine<DemoIdState> {

    @SignaledGuard(allowed = 3)
    void toZero();
    @SignaledGuard(allowed = 0)
    void toOne();
    @SignaledGuard(allowed = 1)
    void toTwo();
    @SignaledGuard(allowed = 2)
    void toThree();
}
