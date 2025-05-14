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

import com.rabbitmq.client.*;
import org.coodex.concrete.amqp.AMQPConnectionConfig;
import org.coodex.concrete.amqp.AMQPConnectionFacade;
import org.coodex.concrete.message.CourierPrototype;
import org.coodex.concrete.message.Serializer;
import org.coodex.concrete.message.Topics;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.DigestHelper;
import org.coodex.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import static org.coodex.concrete.message.Topics.*;

public class RabbitMQCourierPrototype<M extends Serializable> extends CourierPrototype<M> {

    public static final String PREFIX_RABBITMQ = "rabbitmq";
    public static final String KEY_VIRTUAL_HOST = "virtualHost";
    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    @SuppressWarnings("unused")
    public static final String KEY_SSL = "ssl";
    public static final String KEY_ROUTING_KEY = "routingKey";
    public static final String KEY_EXCHANGER = "exchanger";
    @SuppressWarnings("unused")
    public static final String KEY_TTL = "ttl";
    public static final String DEFAULT_EXCHANGER_NAME = "org.coodex.concrete.topics";

    private final static Logger log = LoggerFactory.getLogger(RabbitMQCourierPrototype.class);
    private final Serializer serializer;
    private final Connection connection;
    private final String routingKey;
    private boolean consumer = false;
    private String consumerStr = null;
    private String queueName;
    private Channel channel;
    private String exchangerName;

    public RabbitMQCourierPrototype(String queue, String destination, Type topicType) {
        super(queue, destination, topicType);
        String customRoutingKey = Config.get(KEY_ROUTING_KEY, TAG_QUEUE, queue);
        routingKey = Common.isBlank(customRoutingKey) ? DigestHelper.sha1(
                String.format("%s@%s", ReflectHelper.typeToCodeStr(getTopicType()), queue).getBytes(StandardCharsets.UTF_8)
        ) : (customRoutingKey + "@" + queue);

        // build connection
        try {
            AMQPConnectionConfig connectionConfig = new AMQPConnectionConfig();
            if (!destination.equalsIgnoreCase(PREFIX_RABBITMQ)) {
                connectionConfig.setUri(destination.substring(PREFIX_RABBITMQ.length() + 1));
            }
            connectionConfig.setHost(Config.get(KEY_HOST, TAG_QUEUE, queue));
            connectionConfig.setPort(Config.getValue(KEY_PORT, (Integer) null, TAG_QUEUE, queue));
            connectionConfig.setPassword(Config.get(QUEUE_PA55W0RD, TAG_QUEUE, queue));
            connectionConfig.setUsername(Config.get(QUEUE_USERNAME, TAG_QUEUE, queue));
            connectionConfig.setVirtualHost(Config.get(KEY_VIRTUAL_HOST, TAG_QUEUE, queue));

            serializer = Topics.getSerializer(Config.get(SERIALIZER_TYPE, TAG_QUEUE, queue));
            exchangerName = Config.get(KEY_EXCHANGER, TAG_QUEUE, queue);
            if (Common.isBlank(exchangerName)) {
                exchangerName = DEFAULT_EXCHANGER_NAME;
            }

            connection = AMQPConnectionFacade.getConnection(connectionConfig);
        } catch (Throwable th) {
            throw Common.rte(th);
        }
    }

    @Override
    protected void afterTopicAssociation() {
        if (channel == null) {
            try {
                channel = connection.createChannel();

                channel.exchangeDeclare(exchangerName, BuiltinExchangeType.TOPIC);
                queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, exchangerName, routingKey);
            } catch (Throwable th) {
                throw Common.rte(th);
            }
        }
    }

    @Override
    public void deliver(M message) {
        if (channel != null) {
            try {
                channel.basicPublish(exchangerName, routingKey, null,
                        serializer.serialize(message));
            } catch (IOException e) {
                throw Common.rte(e);
            }
        } else {
            throw new RuntimeException("rabbitmq channel NOT build: " + getQueue());
        }
    }

    @Override
    public synchronized boolean isConsumer() {
        return consumer;
    }

    @Override
    public synchronized void setConsumer(boolean consumer) {
        if (consumer != this.consumer) {
            try {
                if (consumer) {
                    consumerStr = channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                            try {
                                getTopic().notify(serializer.deserialize(body, getMessageType()));
                            } catch (Throwable th) {
                                log.warn("notify error: {}", th.getLocalizedMessage(), th);
                            }
                        }
                    });
                } else if (consumerStr != null) {
                    try {
                        channel.basicCancel(consumerStr);
                    } finally {
                        consumerStr = null;
                    }
                }
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            }
            this.consumer = consumer;
        }
    }

}
