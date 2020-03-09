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

/**
 *
 */
package org.coodex.servlet.cors;

import java.util.StringTokenizer;

/**
 * @author davidoff
 */
public class CORSSetter {

    public static void set(CORSSetting setting, HeaderSetter setter,
                           String origin) {
        Boolean allowCredentials = setting.allowCredentials();
        Long maxAge = setting.maxAge();
        String allowHeaders = setting.allowHeaders();
        String allowMethod = setting.allowMethod();
        String allowOrigin = setting.allowOrigin();
        String exposeHeaders = setting.exposeHeaders();

        if (allowCredentials != null)
            setter.set(CORSSetting.ALLOW_CREDENTIALS,
                    String.valueOf(allowCredentials.booleanValue()));

        if (maxAge != null)
            setter.set(CORSSetting.MAX_AGE, String.valueOf(maxAge.longValue()));

        if (allowHeaders != null)
            setter.set(CORSSetting.ALLOW_HEADERS, allowHeaders);

        if (allowOrigin != null) {
            StringTokenizer st = new StringTokenizer(allowOrigin, ", ", false);
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (s.equals("*") || s.equalsIgnoreCase(origin)) {
                    setter.set(CORSSetting.ALLOW_ORIGIN, origin);
                    break;
                }
            }
        }

        if (allowMethod != null)
            setter.set(CORSSetting.ALLOW_METHOD, allowMethod);

        if (exposeHeaders != null)
            setter.set(CORSSetting.EXPOSE_HEADERS, exposeHeaders);

    }

}
