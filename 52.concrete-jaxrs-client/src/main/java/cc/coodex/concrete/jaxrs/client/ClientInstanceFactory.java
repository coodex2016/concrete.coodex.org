package cc.coodex.concrete.jaxrs.client;

import cc.coodex.concrete.api.ConcreteService;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public interface ClientInstanceFactory {

//    <T extends ConcreteService> T create(Class<? extends T> type);

    <T extends ConcreteService> T create(Class<? extends T> type, String domain);
}
