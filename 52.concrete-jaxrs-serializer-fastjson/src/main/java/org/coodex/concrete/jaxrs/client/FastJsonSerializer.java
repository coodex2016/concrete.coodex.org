package org.coodex.concrete.jaxrs.client;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class FastJsonSerializer implements JSONSerializer {
    @Override
    public <T> T parse(String json, Type t) {
        return JSON.parseObject(json, t);
    }

    @Override
    public String toJson(Object t) {
        return JSON.toJSONString(t);
    }
}
