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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

@SPI.Ordered(0)
public class ProfileProviderBaseYaml /*extends AbstractProfileProvider */ implements ProfileProvider {

    private final static Logger log = LoggerFactory.getLogger(ProfileProviderBaseYaml.class);

    private static final String[] SUPPORTED = new String[]{".yml", ".yaml"};

    private static final String YAML_CLASS = "org.yaml.snakeyaml.Yaml";

    private static final Singleton<Boolean> YAML_SUPPORTED = Singleton.with(() -> {
        try {
            Class.forName(YAML_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            log.info("{} not found. TODO: support other yaml reader", YAML_CLASS);
            return false;
        }
    });

    @Override
    public String[] getSupported() {
        return SUPPORTED;
    }

    @Override
    public boolean isAvailable() {
        return YAML_SUPPORTED.get();
    }

    @Override
    public Profile get(URL url) {
        return new ProfileBaseYaml(url);
    }

//    @Override
//    public int priority() {
//        return 100;
//    }

    @Override
    public boolean accept(URL param) {
        if (param == null || !isAvailable())
            return false;
        String path = param.toString();
        for (String x : SUPPORTED) {
            if (path.endsWith(x)) return true;
        }
        return false;
    }
}
