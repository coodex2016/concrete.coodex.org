package org.coodex.concrete.jaxrs.client;

/**
 * Created by davidoff shen on 2016-12-09.
 */
public interface InvokerFactory {

    boolean accept(String domain);

    Invoker getInvoker(String domain);
}
