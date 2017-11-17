package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.Guard;
import org.coodex.concrete.fsm.StateTransfer;

public interface FSMDemo extends FiniteStateMachine<NumericState> {

    @Guard(ConditionTo0.class)
    void toZero();

    @Guard(ConditionTo1.class)
    void toOne();

    @Guard(ConditionTo2.class)
    void toTwo();

    @Guard(ConditionTo3.class)
    void toThree();
}
