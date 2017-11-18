package org.coodex.concrete.fsm;

import java.io.Serializable;

public interface IdentifiedState<ID extends Serializable> extends State {

    ID getId();

    /**
     * 慎用
     *
     * @param id
     */
    void setId(ID id);
}
