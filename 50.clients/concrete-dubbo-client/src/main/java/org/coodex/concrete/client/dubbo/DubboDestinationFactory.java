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

package org.coodex.concrete.client.dubbo;

import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.AbstractDestinationFactory;
import org.coodex.concrete.client.Destination;
import org.coodex.util.Common;

public class DubboDestinationFactory extends AbstractDestinationFactory<DubboDestination> {

    public static boolean isDubbo(String location) {
        return "dubbo".equalsIgnoreCase(location);
    }

    @Override
    public Destination build(String module) {
        DubboDestination dubboDestination = init(new DubboDestination(), module);
        dubboDestination.setName(ClientHelper.getString(module, "name"));
        dubboDestination.setRegistries(
                Common.toArray(ClientHelper.getString(module,"registry"),",", new String[0])
        );
        return dubboDestination;
    }

    @Override
    public boolean accept(String module) {
        return isDubbo(getLocation(module));
    }
}
