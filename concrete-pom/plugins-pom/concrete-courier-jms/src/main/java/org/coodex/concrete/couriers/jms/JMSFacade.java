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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.message.Serializer;
import org.coodex.concrete.message.Topics;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.message.Topics.*;


class JMSFacade {

    private final static LazySelectableServiceLoader<String, ConnectionFactoryProvider>
            CONNECTION_FACTORY_PROVIDER_SELECTABLE_SERVICE_LOADER =
            new LazySelectableServiceLoader<String, ConnectionFactoryProvider>() {
            };

    private final static SingletonMap<String, ConnectionFactory> connectionFactorySingletonMap
            = SingletonMap.<String, ConnectionFactory>builder()
            .function(key -> {
                ConnectionFactoryProvider cfp = CONNECTION_FACTORY_PROVIDER_SELECTABLE_SERVICE_LOADER.select(key);
                if (cfp == null) {
                    throw new RuntimeException("no ConnectionFactoryProvider found for :" + key);
                } else {
                    return cfp.build(key);
                }
            }).build();

    private final static Logger log = LoggerFactory.getLogger(JMSFacade.class);
    //    private static ScheduledExecutorService scheduledExecutorService = ExecutorsHelper.newSingleThreadScheduledExecutor();
    private final String name;
    private final String driver;
    private final String topicName;
    private final String userName;
    private final String password;
    private final MessageListener messageListener;
    private final Type messageType;
    private final Serializer serializer;
    private final ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageProducer producer;
    private MessageConsumer consumer;

    JMSFacade(String name, String driver, String topicName, final ObjectListener objectListener, Type messageType) {
        this.name = name;
        this.driver = driver;
        this.topicName = topicName;
        this.messageType = messageType;
        this.messageListener = message -> {
            if (message instanceof ObjectMessage) {
                objectListener.receive(deserialize((ObjectMessage) message));
            }
        };
        this.connectionFactory = connectionFactorySingletonMap.get(driver);
        this.userName = Config.get(QUEUE_USERNAME, TAG_QUEUE, name);
        this.password = Config.get(QUEUE_PA55W0RD, TAG_QUEUE, name);
        this.serializer = Topics.getSerializer(Config.get(SERIALIZER_TYPE, TAG_QUEUE, name));
        try {
            connect();
        } catch (JMSException e) {
            reconnect();
        }
    }

    void publish(Serializable message) {
        if (message == null)
            throw new NullPointerException("message is null.");

        if (producer != null) {
            try {
                producer.send(session.createObjectMessage(
                        serializer.serialize(message)
                ));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("cannot publish message, because connection is failed.");
        }
    }

    void setConsumer(boolean isConsumer) throws JMSException {
        if (isConsumer && consumer == null) {
            consumer = session.createConsumer(destination, null, false);
            consumer.setMessageListener(messageListener);
        } else if (!isConsumer && consumer != null) {
            try {
                consumer.close();
            } finally {
                consumer = null;
            }
        }

    }

    private Object deserialize(ObjectMessage message) {
        try {
            Serializable serializable = message.getObject();
            if (serializable instanceof byte[]) {
                return serializer.deserialize((byte[]) serializable, messageType);
            } else {
                throw new RuntimeException("wrong message type: " +
                        (serializable == null ? null : serializable.getClass().toString()));
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void reconnect() {
        clear();
        ConcreteHelper.getScheduler("jms.reconnect").schedule(() -> {
            try {
                connect();
            } catch (JMSException e) {
                log.info("{}: connect {} failed: {}. retry...",
                        name, driver, e.getLocalizedMessage());
                reconnect();
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void connect() throws JMSException {
        connection = Common.isBlank(userName) ?
                connectionFactory.createConnection() :
                connectionFactory.createConnection(userName, password);

        connection.setExceptionListener(e -> reconnect());

        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = session.createTopic(topicName);
        producer = session.createProducer(destination);
    }

    private void clear() {
        if (connection != null) {
            try {
                producer.close();
            } catch (JMSException ignored) {// NOSONAR
            }
            try {
                consumer.close();
            } catch (JMSException ignored) {// NOSONAR
            }
            try {
                session.close();
            } catch (JMSException ignored) {// NOSONAR
            }
            try {
                connection.stop();
            } catch (JMSException ignored) {// NOSONAR
            }
            try {
                connection.close();
            } catch (JMSException ignored) {// NOSONAR
            }
            producer = null;
            consumer = null;
            session = null;
            connection = null;
        }
    }

    interface ObjectListener {
        void receive(Object o);
    }

}
