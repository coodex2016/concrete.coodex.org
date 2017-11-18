package org.coodex.concrete.fsm;


import java.io.Serializable;

public interface IdentifiedStateContainer {


    <S extends IdentifiedState<ID>, ID extends Serializable> S newStateAndLock(Class<? extends S> stateClass);

    <S extends IdentifiedState<ID>, ID extends Serializable> S newStateAndLock(Class<? extends S> stateClass, long timeout);

    <S extends IdentifiedState<ID>, ID extends Serializable> S loadStateAndLock(ID id, Class<? extends S> stateClass) throws IdentifiedStateIsLockingException;

    <S extends IdentifiedState<ID>, ID extends Serializable> S loadStateAndLock(ID id, Class<? extends S> stateClass, long timeoutInMS) throws IdentifiedStateIsLockingException;



    void release(Serializable id);
}
