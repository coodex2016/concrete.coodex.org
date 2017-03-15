package org.coodex.concrete.jaxrs.client;

import java.lang.reflect.Type;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public interface JSONSerializer {

    <T> T parse(String json, Type t);

    String toJson(Object t);
}
