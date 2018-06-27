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

package org.coodex.concrete.couriers.jms;

import org.coodex.concrete.message.CourierPrototype;
import org.coodex.util.Singleton;

import java.io.Serializable;
import java.lang.reflect.Type;

import static org.coodex.concrete.couriers.jms.JMSCourierPrototypeProvider.JMS_PREFIX;

public class JMSCourierPrototype<M extends Serializable> extends CourierPrototype<M> {

    private final String driver;
    private final Singleton<JMSFacade> jmsFacadeSingleton = new Singleton<JMSFacade>(
            new Singleton.Builder<JMSFacade>() {
                @Override
                public JMSFacade build() {

                    return new JMSFacade(getQueue(),
                            driver, String.format("%s@%s",getTopicType().toString(), getQueue()),
                            new JMSFacade.ObjectListener() {
                                @Override
                                public void receive(Object o) {
                                    getTopic().notify((M) o);
                                }
                            });
                }
            }
    );

    public JMSCourierPrototype(String queue, String destination, Type topicType) {
        super(queue, destination, topicType);
        driver = getDriverFromDestination(destination);
    }

    private String getDriverFromDestination(String destination) {
        return destination.substring(JMS_PREFIX.length());
    }


    @Override
    public void deliver(M message) {
        jmsFacadeSingleton.getInstance().publish(message);
    }
}
