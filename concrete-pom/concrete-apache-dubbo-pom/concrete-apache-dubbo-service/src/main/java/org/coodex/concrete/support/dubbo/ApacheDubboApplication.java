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

package org.coodex.concrete.support.dubbo;

import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.registry.RegistryService;
import org.coodex.concrete.dubbo.DubboConfigCaching;
import org.coodex.util.Common;
import org.coodex.util.Singleton;

import java.util.List;
import java.util.Set;

public class ApacheDubboApplication extends AbstractDubboApplication {

    // SimpleRegistryService
    //        ServiceConfig<RegistryService> config = new ServiceConfig<>();
    //        config.setApplication(DubboConfigCaching.getApplicationConfig("simple-registry"));
    //        ProtocolConfig protocolConfig = new ProtocolConfig();
    //        protocolConfig.setPort(9090);
    //        config.setProtocol(protocolConfig);
    //            config.setRef(new SimpleRegistryService());
    // SimpleRegistry
    private static final Singleton<RegistryConfig> simpleRegistry = Singleton.with(DubboConfigCaching::getSimpleRegistry);


    private final List<RegistryConfig> registryConfigs;
    private final List<ProtocolConfig> protocolConfigs;
    private final String version;

    public ApacheDubboApplication(String applicationName, Set<String> registries, Set<String> protocols, String version) {
        super(applicationName);
        registryConfigs = DubboConfigCaching.getRegistries(registries);
        if (registryConfigs.size() == 0) {
            registryConfigs.add(simpleRegistry.get());
        }
        protocolConfigs = DubboConfigCaching.getProtocols(protocols);
        if (protocolConfigs.size() == 0) {
            protocolConfigs.add(DubboConfigCaching.getProtocol("dubbo"));
        }
        this.version = version;
    }


    public ApacheDubboApplication(String applicationName, String[] registries, String[] protocols, String version) {
        this(applicationName, Common.arrayToSet(registries), Common.arrayToSet(protocols), version);
//        super(applicationName);
//        registryConfigs = DubboConfigCaching.getRegistries(registries);
//        protocolConfigs = DubboConfigCaching.getProtocols(protocols);
//        this.version = version;
    }


    @Override
    protected String getVersion() {
        return version;
    }

    @Override
    protected List<RegistryConfig> getRegistries() {
        return registryConfigs;
    }

    @Override
    protected List<ProtocolConfig> getProtocols() {
        return protocolConfigs;
    }
}
