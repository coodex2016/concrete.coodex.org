/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.client.jaxrs.jersey;

import org.coodex.concrete.client.jaxrs.ConfigurationProvider;
import org.coodex.util.Singleton;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.core.Configuration;

public class JerseyConfigurationProvider implements ConfigurationProvider {
    private final Singleton<Configuration> configurationSingleton = Singleton.with(() -> {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        return clientConfig;
    });

    @Override
    public Configuration getConfiguration() {
        return configurationSingleton.get();
    }
}
