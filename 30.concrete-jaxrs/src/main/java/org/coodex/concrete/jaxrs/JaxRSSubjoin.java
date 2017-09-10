/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.Subjoin;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.coodex.util.Common.concat;

/**
 * Created by davidoff shen on 2017-04-20.
 */
public class JaxRSSubjoin implements Subjoin {

    private final Locale locale;
    private final MultivaluedMap<String, String> headers;

    public JaxRSSubjoin(HttpHeaders httpHeaders) {
        if(httpHeaders == null){
            this.locale = Locale.getDefault();
            this.headers = new MultivaluedHashMap<String, String>();
        } else {
            this.locale = httpHeaders.getLanguage();
            this.headers = httpHeaders.getRequestHeaders();
        }
//        add(JaxRSHelper.JAXRS_MODEL, "T");
    }


    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String get(String name) {
        return get(name, "; ");
    }

    @Override
    public String get(String name, String split) {
        return concat(headers.get(name), split);
    }

    @Override
    public List<String> getList(String name) {
        List<String> list = headers.get(name);
        return list == null ? null : new ArrayList<String>(list);
    }

    @Override
    public Set<String> keySet() {
        return headers.keySet();
    }

    @Override
    public void set(String name, List<String> values) {
        throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, "header cannot change.");
    }

    @Override
    public void add(String name, String value) {
        throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, "header cannot change.");
    }

}
