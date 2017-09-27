/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.websocket.client;

import javax.websocket.ClientEndpointConfig.Configurator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteHelper.VERSION;

public class SetUserAgentConfigurator extends Configurator {
    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        super.beforeRequest(headers);
        headers.put("User-Agent", Arrays.asList("concrete-web-socket-client " + VERSION));
    }
}
