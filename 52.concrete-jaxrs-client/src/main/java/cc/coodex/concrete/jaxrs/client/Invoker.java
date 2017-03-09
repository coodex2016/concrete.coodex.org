package cc.coodex.concrete.jaxrs.client;

import cc.coodex.concrete.jaxrs.struct.Unit;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public interface Invoker {

    /**
     * 阻塞模式调用
     *
     * @param unit
     * @param args
     * @return
     * @throws Throwable
     */
    Object invoke(/*String domain,*/ Unit unit, Object[] args, Object instance) throws Throwable;

}
