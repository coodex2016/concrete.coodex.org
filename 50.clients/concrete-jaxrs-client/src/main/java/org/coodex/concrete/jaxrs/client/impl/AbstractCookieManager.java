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

package org.coodex.concrete.jaxrs.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.NewCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-04-15.
 */
@Deprecated
public abstract class AbstractCookieManager {

    private final static Logger log = LoggerFactory.getLogger(AbstractCookieManager.class);


    private Map<String/*path*/, Map<String/*key*/, NewCookie>> cookies = new HashMap<String, Map<String, NewCookie>>();


    public void store(Collection<NewCookie> cookies) {
        synchronized (this.cookies) {
            for (NewCookie cookie : cookies) {
                Map<String, NewCookie> cached = this.cookies.get(cookie.getPath());
                if (cached == null) {
                    cached = new HashMap<String, NewCookie>();
                    this.cookies.put(cookie.getPath(), cached);
                }
                cached.put(cookie.getName(), cookie);
            }
        }
        if (cookies != null && cookies.size() > 0) {
            log.debug("store {} cookie(s).", cookies.size());
        }
    }

    private boolean match(String cookiePath, String urlPath) {
        boolean is = cookiePath.equals(urlPath) || urlPath.startsWith(cookiePath);
        boolean endWith = cookiePath.endsWith("/");
        if (is) {
            return urlPath.charAt(cookiePath.length() - (endWith ? 1 : 0)) == '/';
        }
        return is;
    }

    public Collection<NewCookie> load(String urlPath) {
        Map<String, NewCookie> cookieMap = new HashMap<String, NewCookie>();
        for (String path : this.cookies.keySet()) {
            if (match(path, urlPath)) {
                for (NewCookie cookie : this.cookies.get(path).values()) {
                    if (cookie.getExpiry() == null ||
                            (cookie.getExpiry().getTime() >= System.currentTimeMillis())) {
                        cookieMap.put(cookie.getName(), cookie);
                    } else {
                        this.cookies.get(path).remove(cookie.getName());
                    }
                }
            }
        }
        if (cookieMap.values().size() > 0) {
            log.debug("load {} cookie(s) for {}", cookieMap.values().size(), urlPath);
        }
        return new ArrayList<NewCookie>(cookieMap.values());
    }
}
