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

package test.org.coodex.concrete.amqp;

import com.rabbitmq.client.Connection;
import org.coodex.concrete.amqp.AMQPConnectionConfig;
import org.coodex.concrete.amqp.AMQPConnectionFacade;
import org.coodex.util.Profile;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class AMQPConnFacadeTest {
    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        Profile profile = Profile.getProfile("env.properties");
        AMQPConnectionConfig connectionConfig = new AMQPConnectionConfig();
        connectionConfig.setUri(profile.getString("amqp.location"));
        Connection connection = AMQPConnectionFacade.getConnection(connectionConfig);

        AMQPConnectionConfig connectionConfig2 = new AMQPConnectionConfig();
        connectionConfig2.setHost(profile.getString("amqp.host"));
        connectionConfig2.setPort(profile.getInt("amqp.port"));
        connectionConfig2.setUsername(profile.getString("amqp.username"));
        connectionConfig2.setPassword(profile.getString("amqp.password"));
        Connection connection2 = AMQPConnectionFacade.getConnection(connectionConfig2);
        System.out.println(connection == connection2);

    }
}
