/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.concrete.common;

import org.coodex.util.Common;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class FastJsonSerializer implements JSONSerializer {

    private Class<?> jsonClass = null;

    private Method toJSONString = null;

    private Method parseObject = null;

    private Class<?> feature = null;

    private Object ignoreNotMatch = null;

//    public static void main(String[] args) {
//        System.out.println(JSONSerializerFactory.getInstance().toJson("ok"));
//        System.out.println(JSONSerializerFactory.getInstance().<String>parse("ok", String.class));
//    }

    private synchronized void init() throws ClassNotFoundException, NoSuchMethodException {
        if (jsonClass == null) {
            jsonClass = Class.forName("com.alibaba.fastjson.JSON");
            feature = Class.forName("com.alibaba.fastjson.parser.Feature");
            for (Object e : feature.getEnumConstants()) {
                if ("IgnoreNotMatch".equals(e.toString())) {
                    ignoreNotMatch = e;
                    break;
                }
            }
            toJSONString = jsonClass.getMethod("toJSONString", Object.class);
            parseObject = jsonClass.getMethod("parseObject", String.class, Type.class, Array.newInstance(feature, 0).getClass());
        }
    }

    private Object $parse(String json, Type t) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        init();
        Object array = Array.newInstance(feature, 1);
        Array.set(array, 0, ignoreNotMatch);
        return parseObject.invoke(null, json, t, array);
    }

    private String $toJson(Object t) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
        init();
        return (String) toJSONString.invoke(null, t);
    }

    @Override
    public <T> T parse(String json, Type t) {
        try {
            return /*String.class.equals(t) ? Common.cast(json) :*/ Common.cast($parse(json, t));
            //JSON.parseObject(json, t, Feature.IgnoreNotMatch);
        } catch (Throwable th) {
            throw th instanceof RuntimeException ? (RuntimeException) th : new RuntimeException(th);
        }
    }

    @Override
    public String toJson(Object t) {
        try {
            return $toJson(t);
            //JSON.toJSONString(t);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
