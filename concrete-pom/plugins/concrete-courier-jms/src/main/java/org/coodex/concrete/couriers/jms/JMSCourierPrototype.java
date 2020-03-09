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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.io.Serializable;
import java.lang.reflect.Type;

import static org.coodex.concrete.couriers.jms.JMSCourierPrototypeProvider.JMS_PREFIX;

public class JMSCourierPrototype<M extends Serializable> extends CourierPrototype<M> {

    private final static Logger log = LoggerFactory.getLogger(JMSCourierPrototype.class);

    private final String driver;
    private final Singleton<JMSFacade> jmsFacadeSingleton = new Singleton<JMSFacade>(
            new Singleton.Builder<JMSFacade>() {
                @Override
                public JMSFacade build() {

                    //noinspection unchecked
                    return new JMSFacade(getQueue(),
                            driver, String.format("%s@%s", getTopicType().toString(), getQueue()),
                            o -> getTopic().notify((M) o), getMessageType());
                }
            }
    );
    private boolean consumer = false;

    public JMSCourierPrototype(String queue, String destination, Type topicType) {
        super(queue, destination, topicType);
        driver = getDriverFromDestination(destination);
    }

    @Override
    protected void afterTopicAssociation() {
        jmsFacadeSingleton.get();
    }

    private String getDriverFromDestination(String destination) {
        return destination.substring(JMS_PREFIX.length());
    }


    @Override
    public void deliver(M message) {
        jmsFacadeSingleton.get().publish(message);
    }

    @Override
    public synchronized boolean isConsumer() {
        return consumer;
    }

    @Override
    public synchronized void setConsumer(boolean consumer) {
        if (consumer != this.consumer) {
            try {
                jmsFacadeSingleton.get().setConsumer(consumer);
                this.consumer = consumer;
            } catch (JMSException e) {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }
}
