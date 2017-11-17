package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.StateTransfer;

public interface FSMDemo extends FiniteStateMachine<NumericState> {

    @StateTransfer(ConditionTo0.class)
    void toZero();

    @StateTransfer(ConditionTo1.class)
    void toOne();

    @StateTransfer(ConditionTo2.class)
    void toTwo();

    @StateTransfer(ConditionTo3.class)
    void toThree();
}
