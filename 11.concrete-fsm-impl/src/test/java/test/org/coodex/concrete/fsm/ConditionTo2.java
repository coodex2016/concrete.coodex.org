package test.org.coodex.concrete.fsm;

import org.coodex.concrete.fsm.StateCondition;

public class ConditionTo2 implements StateCondition<NumericState> {
    @Override
    public boolean allow(NumericState state) {
        return state.getValue() == 1;
    }
}
