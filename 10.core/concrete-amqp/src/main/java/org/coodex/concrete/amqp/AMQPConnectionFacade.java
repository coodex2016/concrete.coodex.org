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

package org.coodex.concrete.amqp;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.coodex.concrete.common.AbstractCopier;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class AMQPConnectionFacade {


    private static AbstractCopier<ConnectionFactory, AMQPConnectionConfig> configCopier =
            new AbstractCopier<ConnectionFactory, AMQPConnectionConfig>() {
                @Override
                public AMQPConnectionConfig copy(ConnectionFactory connectionFactory, AMQPConnectionConfig amqpConnectionConfig) {
                    amqpConnectionConfig.setPort(connectionFactory.getPort());
                    amqpConnectionConfig.setHost(connectionFactory.getHost());
                    amqpConnectionConfig.setVirtualHost(connectionFactory.getVirtualHost());
                    amqpConnectionConfig.setPassword(connectionFactory.getPassword());
                    amqpConnectionConfig.setUsername(connectionFactory.getUsername());
                    return amqpConnectionConfig;
                }
            };

    private static SingletonMap<AMQPConnectionConfig, Connection> connectionSingletonMap =
            new SingletonMap<AMQPConnectionConfig, Connection>(new SingletonMap.Builder<AMQPConnectionConfig, Connection>() {
                @Override
                public Connection build(AMQPConnectionConfig key) {
                    try {
                        return toConnectionFactory(key).newConnection();
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Throwable th) {
                        throw new RuntimeException(th.getLocalizedMessage(), th);
                    }
                }
            });

    private static AMQPConnectionConfig clean(AMQPConnectionConfig amqpConnectionConfig) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        return configCopier.copy(toConnectionFactory(amqpConnectionConfig));
    }

    private static ConnectionFactory toConnectionFactory(String uri, String virtualHost, String host, Integer port,
                                                         String username, String password) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        if (Common.isBlank(uri)) {
            connectionFactory.setVirtualHost(virtualHost);
            connectionFactory.setHost(host);
            if (port != null)
                connectionFactory.setPort(port);
        } else {
            connectionFactory.setUri(uri);
        }
        if (Common.isBlank(connectionFactory.getVirtualHost())) {
            connectionFactory.setVirtualHost("/");
        }
        if (!Common.isBlank(username) ||
                !Common.isBlank(password)) {
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
        }
        connectionFactory.setAutomaticRecoveryEnabled(true);
        return connectionFactory;
    }

    private static ConnectionFactory toConnectionFactory(AMQPConnectionConfig amqpConnectionConfig) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        return toConnectionFactory(amqpConnectionConfig.getUri(),
                amqpConnectionConfig.getVirtualHost(),
                amqpConnectionConfig.getHost(),
                amqpConnectionConfig.getPort(),
                amqpConnectionConfig.getUsername(),
                amqpConnectionConfig.getPassword());
    }

    public static Connection getConnection(AMQPConnectionConfig connectionConfig) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        return connectionSingletonMap.getInstance(clean(connectionConfig));
    }

}
