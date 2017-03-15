package org.coodex.concrete.jaxrs.client;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-12-08.
 */
class CookieManager implements CookieJar {

    private Map<String/*path*/, Map<String/*key*/, Cookie>> cookies = new HashMap<String, Map<String, Cookie>>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        synchronized (this.cookies) {
            for (Cookie cookie : cookies) {
                Map<String, Cookie> cached = this.cookies.get(cookie.path());
                if (cached == null) {
                    cached = new HashMap<String, Cookie>();
                    this.cookies.put(cookie.path(), cached);
                }
                cached.put(cookie.name(), cookie);
            }
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

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        String urlPath = url.encodedPath();
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        for (String path : this.cookies.keySet()) {
            if (match(path, urlPath)) {
                for (Cookie cookie : this.cookies.get(path).values()) {
                    if (cookie.expiresAt() >= System.currentTimeMillis()) {
                        cookieMap.put(cookie.name(), cookie);
                    } else {
                        this.cookies.get(path).remove(cookie.name());
                    }
                }
            }
        }
        return new ArrayList<Cookie>(cookieMap.values());
    }
}
