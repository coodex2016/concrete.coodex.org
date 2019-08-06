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

package org.coodex.util;

import java.net.URL;

public class ProfileProviderBaseProperties extends AbstractProfileProvider {
    private static String[] SUPPORTED = new String[]{".properties"};

    @Override
    public String[] getSupported() {
        return SUPPORTED;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Profile get(URL url) {
        return new ProfileBaseProperties(url);
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public boolean accept(URL param) {
        return param != null && param.toString().endsWith(SUPPORTED[0]);
    }
}
