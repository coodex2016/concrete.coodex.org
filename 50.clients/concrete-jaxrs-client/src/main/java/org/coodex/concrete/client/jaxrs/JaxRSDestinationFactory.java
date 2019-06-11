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

package org.coodex.concrete.client.jaxrs;

import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.AbstractDestinationFactory;
import org.coodex.concrete.client.Destination;
import org.coodex.util.Common;

public class JaxRSDestinationFactory extends AbstractDestinationFactory<JaxRSDestination> {

    public static boolean isJaxRS(String location) {
        return location.toLowerCase().startsWith("http://")
                || isSSL(location);
    }

    public static boolean isSSL(String location) {
        return location.toLowerCase().startsWith("https://");
    }

    @Override
    public Destination build(String module) {
        JaxRSDestination destination = init(new JaxRSDestination(), module, false);
        destination.setLogLevel(ClientHelper.getString(module, "logLevel"));
        destination.setSsl(ClientHelper.getString(module, "ssl"));
        destination.setCharset(ClientHelper.getString(module, "jaxrs.charset"));
//        destination.setAsync(
//                Common.toBool(ConcreteHelper.getString(TAG_CLIENT, module, "async"), true)
//        );
        return destination;
    }

    @Override
    public boolean accept(String param) {
        String location = getLocation(param);
        return !Common.isBlank(location) && isJaxRS(location);
    }
}
