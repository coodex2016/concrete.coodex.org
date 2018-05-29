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

package org.coodex.concrete.client;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.util.Common;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class ClientCommon {

    private static final Map<String, String> CLIENT_TOKEN_MANAGER = new HashMap<String, String>();

    private static String getKey(String domain) {
        return Common.isBlank(domain) ? "" : ("." + domain);
    }

    public static Domain getDomain(String domain) {
        return new Domain(
                getDomainIdentify(domain),
                getDomainType(domain),
                getDomainAsyncSupport(domain));
    }

    private static boolean getDomainAsyncSupport(String domain) {
        return ConcreteHelper.getProfile()
                .getBool(
                        "concrete.client" + getKey(domain) + ".async", true);
    }

    private static String getDomainType(String domain) {
        return ConcreteHelper.getProfile()
                .getString(
                        "concrete.client" + getKey(domain) + ".type");
    }

    private static String getDomainIdentify(String domain) {
        String s = ConcreteHelper.getProfile()
                .getString(
                        "concrete.client" + getKey(domain) + ".domain",
                        Common.nullToStr(domain)).trim();
        char[] buf = s.toCharArray();
        int len = buf.length;
        while (len > 0 && buf[len - 1] == '/') {
            len--;
        }
        s = new String(buf, 0, len);
        return s;
    }

    public static void setTokenId(String key, String tokenId) {
        if (!Common.isBlank(key)) CLIENT_TOKEN_MANAGER.put(key, tokenId);
    }

    public static String getTokenId(String key) {
        return Common.isBlank(key) ? null : CLIENT_TOKEN_MANAGER.get(key);
    }

    public static class Domain {
        private String identify;
        private String type;
        private boolean asyncSupport;

        public Domain(String location, String type, boolean asyncSupport) {
            this.identify = location;
            this.type = type;
            this.asyncSupport = asyncSupport;
        }

        public boolean isAsyncSupport() {
            return asyncSupport;
        }

        public String getIdentify() {
            return identify;
        }

        public String getType() {
            return type;
        }
    }
}
