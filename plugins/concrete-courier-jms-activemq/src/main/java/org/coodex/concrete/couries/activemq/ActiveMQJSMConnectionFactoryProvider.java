/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.couries.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.coodex.concrete.couriers.jms.ConnectionFactoryProvider;

import javax.jms.ConnectionFactory;

public class ActiveMQJSMConnectionFactoryProvider implements ConnectionFactoryProvider {

    public static final String PREFIX_ACTIVEMQ = "activemq:";

    @Override
    public ConnectionFactory build(String driver) {
        return new ActiveMQConnectionFactory(driver.substring(PREFIX_ACTIVEMQ.length()));
    }

    @Override
    public boolean accept(String param) {
        return param != null && param.length() > PREFIX_ACTIVEMQ.length()
                && param.substring(0, PREFIX_ACTIVEMQ.length()).equalsIgnoreCase(PREFIX_ACTIVEMQ);
    }
}
