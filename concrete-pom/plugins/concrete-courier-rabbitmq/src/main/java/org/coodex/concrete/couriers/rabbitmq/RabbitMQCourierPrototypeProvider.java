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

package org.coodex.concrete.couriers.rabbitmq;

import org.coodex.concrete.message.CourierPrototype;
import org.coodex.concrete.message.CourierPrototypeProvider;

import static org.coodex.concrete.couriers.rabbitmq.RabbitMQCourierPrototype.PREFIX_RABBITMQ;

public class RabbitMQCourierPrototypeProvider implements CourierPrototypeProvider {
    @Override
    public Class<? extends CourierPrototype> getPrototype() {
        return RabbitMQCourierPrototype.class;
    }

    @Override
    public boolean accept(String param) {
        return param != null && param.length() >= PREFIX_RABBITMQ.length()
                && param.substring(0, PREFIX_RABBITMQ.length()).equalsIgnoreCase(PREFIX_RABBITMQ);
    }
}
