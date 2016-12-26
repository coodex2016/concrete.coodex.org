package cc.coodex.concrete.common;

import cc.coodex.closure.Closure;

/**
 * Created by davidoff shen on 2016-09-06.
 */
public abstract class ConcreteClosure implements Closure {

    public abstract Object concreteRun() throws Throwable;

    @Override
    public final Object run() {
        try {
            return concreteRun();
        } catch (Throwable t) {
            throw ConcreteHelper.getException(t);
        }
    }
}
