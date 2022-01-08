/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.AbstractChangeableSubjoin;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.coodex.concrete.common.ConcreteContext.KEY_TOKEN;
import static org.coodex.concrete.jaxrs.JaxRSHelper.KEY_CLIENT_PROVIDER;

/**
 * Created by davidoff shen on 2017-04-20.
 */
public class JaxRSSubjoin extends AbstractChangeableSubjoin {


    public JaxRSSubjoin(HttpHeaders httpHeaders) {
        super();
        loadFromHeader(httpHeaders.getRequestHeaders());
    }

    public JaxRSSubjoin(MultivaluedMap<String, ?> multivaluedMap) {
        loadFromHeader(multivaluedMap);
    }

    private static void appendStringTo(List<String> builder, Object obj) {
        if (obj instanceof String) {
            builder.add((String) obj);
        } else if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            collection.forEach(el -> appendStringTo(builder, el));
        } else if (obj != null) {
            if (obj.getClass().isArray()) {
                for (int i = 0, l = Array.getLength(obj); i < l; i++) {
                    appendStringTo(builder, Array.get(obj, i));
                }
//                Object[] array = Common.cast(obj);
//                for (Object el : array) {
//                    appendStringTo(builder, obj);
//                }
            } else {
                builder.add(obj.toString());
            }
        }
    }

    private static List<String> toStringList(List<?> list) {
        List<String> result = new ArrayList<>();
        appendStringTo(result, list);
        return result;
    }

    private void loadFromHeader(MultivaluedMap<String, ?> multivaluedMap) {
        Collection<String> skipKeys = skipKeys();
        for (String key : multivaluedMap.keySet()) {
            String lowerKey = key.toLowerCase();
            if (skipKeys == null || !skipKeys.contains(lowerKey))
                set(lowerKey, toStringList(multivaluedMap.get(key)));
        }
    }

    // https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
    // Each header field consists of a name followed by a colon (":") and the field value. Field names are case-insensitive
    //
    @Override
    protected boolean containsKey(String name) {
        return super.containsKey(name) || super.containsKey(name.toLowerCase());
    }

    @Override
    public List<String> getList(String name) {
        if (name == null) return null;
        List<String> values = super.getList(name);
        return values == null ? super.getList(name.toLowerCase()) : values;
    }

    @Override
    protected Collection<String> skipKeys() {

        return Arrays.asList(
                KEY_TOKEN.toLowerCase(), KEY_CLIENT_PROVIDER.toLowerCase(),
                HttpHeaders.USER_AGENT.toLowerCase(),
                HttpHeaders.HOST.toLowerCase(),
                HttpHeaders.ACCEPT.toLowerCase());
    }
}
