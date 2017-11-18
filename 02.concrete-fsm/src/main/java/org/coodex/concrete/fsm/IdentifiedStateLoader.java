package org.coodex.concrete.fsm;

import java.io.Serializable;

public interface IdentifiedStateLoader<S extends IdentifiedState<ID>, ID extends Serializable> {
    S newState();

    S getState(ID id);
}
