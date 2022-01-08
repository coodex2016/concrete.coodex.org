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

import com.rabbitmq.client.*;
import org.coodex.concrete.amqp.AMQPConnectionConfig;
import org.coodex.concrete.amqp.AMQPConnectionFacade;
import org.coodex.concrete.amqp.AMQPHelper;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.impl.AbstractOwnRxInvoker;
import org.coodex.concrete.client.impl.OwnRXMessageListener;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.logging.Level;
import org.coodex.concrete.own.OwnServiceUnit;
import org.coodex.concrete.own.RequestPackage;
import org.coodex.config.Config;
import org.coodex.id.IDGenerator;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.coodex.concrete.amqp.AMQPConstants.*;
import static org.coodex.concrete.amqp.AMQPHelper.getExchangeName;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

public class AMQPInvoker extends AbstractOwnRxInvoker {


//    private static Map<String, AMQPFacade> facadeMap = new HashMap<String, AMQPFacade>();

    private final static Logger log = LoggerFactory.getLogger(AMQPInvoker.class);
    private static final SingletonMap<AMQPDestination, Facade> facadeSingletonMap = SingletonMap.<AMQPDestination, Facade>builder().function(
            key -> {
                try {
                    return new Facade(getConnection(key), key.getExchangeName());
                } catch (RuntimeException re) {
                    throw re;
                } catch (Throwable th) {
                    throw new RuntimeException(th.getLocalizedMessage(), th);
                }
            }
    ).build();
    private final Level level;

    AMQPInvoker(AMQPDestination destination) {
        super(destination);
        level = Level.parse(
                Config.getValue("client", "DEBUG", "amqp.logger.level", getAppSet())
        );
//        exchangeName = IF.isNull(
//                ConcreteHelper.getString(TAG_CLIENT,destination.getIdentify(),)
//        );
    }

//    private final String exchangeName;

    private static Connection getConnection(AMQPDestination destination) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        AMQPConnectionConfig config = new AMQPConnectionConfig();
        if (destination.getLocation().length() > 7) // "amqp://"
            config.setUri(destination.getLocation());
        config.setHost(destination.getHost());
        config.setPort(destination.getPort());
        config.setPassword(destination.getPassword());
        config.setUsername(destination.getUsername());
        config.setVirtualHost(destination.getVirtualHost());
        config.setSharedExecutorName(destination.getSharedExecutorName());
        return AMQPConnectionFacade.getConnection(config);
    }

    @Override
    protected ClientSideContext getContext() {
        ServiceContext context = ConcreteContext.getServiceContext();
        if (context instanceof AMQPClientContext)
            return (AMQPClientContext) context;
        else
            throw new RuntimeException("context [" + context + "] is NOT AMQPClientContext");
    }

    @Override
    protected OwnServiceUnit findUnit(DefinitionContext context) {
        return AMQPHelper.findUnit(context);
    }

    @Override
    protected Level getLoggingLevel() {
        return level;
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected void send(RequestPackage<?> requestPackage) throws Throwable {
        // 1 set client info
        requestPackage.getSubjoin().put(SUBJOIN_KEY_CLIENT_PROVIDER,
                "concrete-amqp-client-" + ConcreteHelper.VERSION);
        // 2 send
        facadeSingletonMap.get((AMQPDestination) getDestination())
                .send(getSerializer().toJson(requestPackage));
    }

    @Override
    public ServiceContext buildContext(DefinitionContext context) {
        return new AMQPClientContext(getDestination(), context);
    }

    //    @Override
//    public ServiceContext buildContext(Class concreteClass, Method method) {
//        return new AMQPClientContext(getDestination(),
//                ConcreteHelper.getDefinitionContext(concreteClass, method));
//    }

    private static class Facade {
        private final Channel channel;
        private final String exchangeName;
        private final String clientId = IDGenerator.newId();

        Facade(final Connection connection, String exchangeName) throws IOException {
            this.exchangeName = getExchangeName(exchangeName);
            channel = connection.createChannel();
            channel.exchangeDeclare(this.exchangeName, BuiltinExchangeType.TOPIC);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, this.exchangeName, ROUTE_KEY_RESPONSE + clientId);
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                    log.info("consumerTag: {}, envelope: {}", consumerTag, envelope.getRoutingKey());
                    OwnRXMessageListener.getInstance().onMessage(new String(body, StandardCharsets.UTF_8));
                }
            });
        }

        void send(String message) throws IOException {
            channel.basicPublish(exchangeName, ROUTE_KEY_REQUEST + clientId,
                    null, message.getBytes(StandardCharsets.UTF_8));
        }
    }
}
