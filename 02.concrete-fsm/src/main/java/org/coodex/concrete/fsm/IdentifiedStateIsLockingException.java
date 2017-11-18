package org.coodex.concrete.fsm;

import java.io.Serializable;

public class IdentifiedStateIsLockingException extends Exception {
    private Serializable id;

    public IdentifiedStateIsLockingException(Serializable id) {
        super("identifiedState in using: " + id);
        this.id = id;
    }

    public Serializable getId() {
        return id;
    }
}
