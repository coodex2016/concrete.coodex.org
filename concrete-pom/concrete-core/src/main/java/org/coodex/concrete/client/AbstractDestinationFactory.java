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

package org.coodex.concrete.client;

import org.coodex.config.Config;
import org.coodex.util.SingletonMap;
import org.coodex.util.UUIDHelper;

import static org.coodex.concrete.client.Destination.DEFAULT_REQUEST_TIMEOUT;
import static org.coodex.concrete.common.ConcreteHelper.KEY_DESTINATION;
import static org.coodex.concrete.common.ConcreteHelper.TAG_CLIENT;

public abstract class AbstractDestinationFactory<T extends Destination> implements DestinationFactory<T, String> {

    private static final SingletonMap<String, String> moduleLocationMap = SingletonMap.<String, String>builder()
            .function(module -> Config.get(KEY_DESTINATION, TAG_CLIENT, module))
            .nullKey("null_" + UUIDHelper.getUUIDString()).build();

    protected String getLocation(String module) {
        return moduleLocationMap.get(module);
    }

    protected T init(T destination, String module/*, boolean defaultAsync*/) {
        destination.setIdentify(module);
        destination.setLocation(getLocation(module));
        destination.setTokenManagerKey(Config.get("tokenManagerKey", TAG_CLIENT, module));
        destination.setTokenTransfer(Config.getValue("tokenTransfer", false, TAG_CLIENT, module));
        destination.setTimeout(Config.getValue("timeout", DEFAULT_REQUEST_TIMEOUT, TAG_CLIENT, module));
        return destination;
    }
}
