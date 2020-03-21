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

package org.coodex.concrete.dubbo;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DubboConfigCaching {

    public static final String DEFAULT_APPLICATION_NAME = "concrete-dubbo-application";
    public static final String DEFAULT_VERSION = "1.0.0";

    private static final SingletonMap<String, ApplicationConfig> applicationConfigs
            = SingletonMap.<String, ApplicationConfig>builder().function(ApplicationConfig::new).build();

    private static final SingletonMap<String, RegistryConfig> registryConfigs
            = SingletonMap.<String, RegistryConfig>builder().function(RegistryConfig::new).build();

    private static final SingletonMap<String, ProtocolConfig> protocolConfigs
            = SingletonMap.<String, ProtocolConfig>builder().function(name -> {
        // TODO 根据不同的协议进行解析，暂定为protocol:port，未设置port时则使用默认端口
        int index = name.indexOf(':');
        return index > 0 ?
                new ProtocolConfig(name.substring(0, index), Integer.parseInt(name.substring(index + 1))) :
                new ProtocolConfig(name);

    }).build();


    public static String getServiceVersion(String version) {
        return Common.isBlank(version) ? DEFAULT_VERSION : version;
    }

    public static ApplicationConfig getApplicationConfig(String name) {
        return applicationConfigs.get(Common.isBlank(name) ? DEFAULT_APPLICATION_NAME : name);
    }

    public static List<RegistryConfig> getRegistries(Collection<String> registries) {
        return registryConfigs.fill(new ArrayList<>(), registries);
    }

    public static List<RegistryConfig> getRegistries(String[] registry) {
        return getRegistries(Arrays.asList(registry));
    }

    public static RegistryConfig getSimpleRegistry() {
        return registryConfigs.get("localhost:9090");
    }

    public static ProtocolConfig getProtocol(String protocol) {
        return protocolConfigs.get(protocol);
    }

    public static List<ProtocolConfig> getProtocols(Collection<String> protocols) {
        return protocolConfigs.fill(new ArrayList<>(), protocols);
    }

    public static List<ProtocolConfig> getProtocols(String[] protocols) {
        return getProtocols(Arrays.asList(protocols));
    }
}
