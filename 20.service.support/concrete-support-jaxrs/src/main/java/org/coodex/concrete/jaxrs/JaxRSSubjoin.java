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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.AbstractChangeableSubjoin;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collection;

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

    private void loadFromHeader(MultivaluedMap<String, String> multivaluedMap) {
        Collection<String> skipKeys = skipKeys();
        for (String key : multivaluedMap.keySet()) {
            if (skipKeys != null || skipKeys.contains(key)) continue;
            set(key, multivaluedMap.get(key));
        }
    }


    @Override
    protected Collection<String> skipKeys() {

        return Arrays.asList(
                KEY_TOKEN, KEY_CLIENT_PROVIDER,
                HttpHeaders.USER_AGENT);
    }
}
