package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.StateCondition;

public class ConditionTo0 implements StateCondition<NumericState> {
    @Override
    public boolean allow(NumericState state) {
        return state.getValue() == 3;
    }
}
