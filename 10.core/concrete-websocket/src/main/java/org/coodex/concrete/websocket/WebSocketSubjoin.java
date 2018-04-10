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

package org.coodex.concrete.websocket;

import org.coodex.concrete.common.SubjoinBaseJava7;

import java.util.Map;

public class WebSocketSubjoin extends SubjoinBaseJava7 {


    public WebSocketSubjoin(Map<String, String> map) {
        super(map);
//        add(WEB_SOCKET_MODEL, "T");
//        if (map == null) return;
//
//        for (String key : map.keySet()) {
//            String v = map.get(key);
//            if (v == null) continue;
//
//            if (key.equalsIgnoreCase(LOCALE)) {
//                setLocale(Locale.forLanguageTag(v));
//            } else {
//                set(key, Common.toArray(v, "; ", new ArrayList<String>()));
//            }
//        }
    }

//    @Override
//    protected Locale forLanguageTag(String localeStr) {
//        return Locale.forLanguageTag(localeStr);
//    }
//
//    @Override
//    protected String toLanguageTag() {
//        return getLocale().toLanguageTag();
//    }
}
