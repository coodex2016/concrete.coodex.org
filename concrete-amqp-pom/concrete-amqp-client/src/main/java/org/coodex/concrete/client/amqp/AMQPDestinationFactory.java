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

package org.coodex.concrete.client.amqp;

import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.AbstractDestinationFactory;
import org.coodex.util.Common;

public class AMQPDestinationFactory extends AbstractDestinationFactory<AMQPDestination> {
    private static final String AMQP = "amqp";

    static boolean isAmqp(String location) {
        if (location == null || location.length() < AMQP.length()) return false;
        return location.toLowerCase().startsWith(AMQP);
    }

    @Override
    public AMQPDestination build(String module) {
        AMQPDestination destination = init(new AMQPDestination(), module);
        destination.setHost(ClientHelper.getString(module, "amqp.host"));
        String port = ClientHelper.getString(module, "amqp.port");
        if (!Common.isBlank(port)) {
            try {
                destination.setPort(Integer.valueOf(port));
            } catch (Throwable t) {
                // parse exception
            }
        }
        destination.setVirtualHost(ClientHelper.getString(module, "amqp.virtualHost"));
        destination.setUsername(ClientHelper.getString(module, "amqp.username"));
        destination.setPassword(ClientHelper.getString(module, "amqp.password"));
        destination.setExchangeName(ClientHelper.getString(module, "amqp.exchange"));
        destination.setSharedExecutorName(ClientHelper.getString(module, "amqp.executorName"));
        return destination;
    }

    @Override
    public boolean accept(String module) {
        return isAmqp(getLocation(module));
    }
}
