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

package org.coodex.concrete.jaxrs.client;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.coodex.concrete.common.AbstractCopier;
import org.coodex.concrete.common.Copier;
import org.coodex.concrete.jaxrs.client.impl.AbstractCookieManager;

import javax.ws.rs.core.NewCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2016-12-08.
 */
class CookieManager extends AbstractCookieManager implements CookieJar {

    //    private Map<String/*path*/, Map<String/*key*/, Cookie>> cookies = new HashMap<String, Map<String, Cookie>>();
    private static final Copier<Cookie, NewCookie> saveCopier = new AbstractCopier<Cookie, NewCookie>() {
        @Override
        public NewCookie copy(Cookie cookie) {
            int maxAge = NewCookie.DEFAULT_MAX_AGE;
            if (cookie.expiresAt() > System.currentTimeMillis()) {
                maxAge = (int) ((cookie.expiresAt() - System.currentTimeMillis()) / 1000);
            }
            NewCookie newCookie = new NewCookie(
                    cookie.name(), cookie.value(), cookie.path(),
                    cookie.domain(), null, maxAge, false, false);
            return newCookie;
        }

        @Override
        public NewCookie copy(Cookie cookie, NewCookie newCookie) {
            return newCookie;
        }
    };

    private static final Copier<NewCookie, Cookie> loadCopier = new AbstractCopier<NewCookie, Cookie>() {

        @Override
        public Cookie copy(NewCookie newCookie) {
            Cookie.Builder builder = new Cookie.Builder();
            return builder.domain(newCookie.getDomain())
                    .name(newCookie.getName())
                    .value(newCookie.getValue()).build();
        }

        @Override
        public Cookie copy(NewCookie newCookie, Cookie cookie) {
            return cookie;
        }
    };

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        store(saveCopier.copy(cookies));

//        synchronized (this.cookies) {
//            for (Cookie cookie : cookies) {
//                Map<String, Cookie> cached = this.cookies.get(cookie.path());
//                if (cached == null) {
//                    cached = new HashMap<String, Cookie>();
//                    this.cookies.put(cookie.path(), cached);
//                }
//                cached.put(cookie.name(), cookie);
//            }
//        }
    }

//    private boolean match(String cookiePath, String urlPath) {
//        boolean is = cookiePath.equals(urlPath) || urlPath.startsWith(cookiePath);
//        boolean endWith = cookiePath.endsWith("/");
//        if (is) {
//            return urlPath.charAt(cookiePath.length() - (endWith ? 1 : 0)) == '/';
//        }
//        return is;
//    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        cookies.addAll(loadCopier.copy(load(url.encodedPath())));
        return cookies;
//        String urlPath = url.encodedPath();
//        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
//        for (String path : this.cookies.keySet()) {
//            if (match(path, urlPath)) {
//                for (Cookie cookie : this.cookies.get(path).values()) {
//                    if (cookie.expiresAt() >= System.currentTimeMillis()) {
//                        cookieMap.put(cookie.name(), cookie);
//                    } else {
//                        this.cookies.get(path).remove(cookie.name());
//                    }
//                }
//            }
//        }
//        return new ArrayList<Cookie>(cookieMap.values());
    }
}
