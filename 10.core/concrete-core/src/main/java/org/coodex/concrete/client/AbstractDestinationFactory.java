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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;
import org.coodex.util.StringKeySingletonMap;

import static org.coodex.concrete.client.Destination.DEFAULT_REQUEST_TIMEOUT;
import static org.coodex.concrete.common.ConcreteHelper.KEY_LOCATION;
import static org.coodex.concrete.common.ConcreteHelper.TAG_CLIENT;

public abstract class AbstractDestinationFactory<T extends Destination> implements DestinationFactory<T, String> {

    private static final SingletonMap<String, String> moduleLocationMap = new StringKeySingletonMap<>(
            module -> ConcreteHelper.getString(TAG_CLIENT, module, KEY_LOCATION));

    protected String getLocation(String module) {
        return moduleLocationMap.get(module);
    }

    protected T init(T destination, String module/*, boolean defaultAsync*/) {
        destination.setIdentify(module);
        destination.setLocation(getLocation(module));
        destination.setTokenManagerKey(ConcreteHelper.getString(TAG_CLIENT, module, "tokenManagerKey"));
        destination.setTokenTransfer(
                Common.toBool(ConcreteHelper.getString(TAG_CLIENT, module, "tokenTransfer"), false)
        );
        destination.setTimeout(
                Common.toInt(ConcreteHelper.getString(TAG_CLIENT, module, "timeout"), DEFAULT_REQUEST_TIMEOUT)
        );
//        destination.setAsync(
//                Common.toBool(ConcreteHelper.getString(TAG_CLIENT, module, "async"), defaultAsync)
//        );
        return destination;
    }
}
