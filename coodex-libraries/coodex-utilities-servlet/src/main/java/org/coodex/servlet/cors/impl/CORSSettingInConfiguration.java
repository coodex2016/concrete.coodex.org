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

package org.coodex.servlet.cors.impl;

import org.coodex.config.Config;
import org.coodex.servlet.cors.CORSSetting;

public class CORSSettingInConfiguration implements CORSSetting {
    private static final String NAMESPACE_CORS = "cors_settings";

    @Override
    public String allowOrigin() {
        return Config.get(KEY_ALLOW_ORIGIN, NAMESPACE_CORS);
    }

    @Override
    public String exposeHeaders() {
        return Config.get(KEY_EXPOSE_HEADERS, NAMESPACE_CORS);
    }

    @Override
    public String allowMethod() {
        return Config.get(KEY_ALLOW_METHOD, NAMESPACE_CORS);
    }

    @Override
    public String allowHeaders() {
        return Config.get(KEY_ALLOW_HEADERS, NAMESPACE_CORS);
    }

    @Override
    public Long maxAge() {
        try {
            return Long.valueOf(Config.get(KEY_MAX_AGE, NAMESPACE_CORS));
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public Boolean allowCredentials() {
        return Config.getValue(KEY_ALLOW_CREDENTIALS, false, NAMESPACE_CORS);
    }
}
