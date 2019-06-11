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

package org.coodex.concrete.support.amqp;

import com.rabbitmq.client.*;
import org.coodex.concrete.amqp.AMQPConnectionConfig;
import org.coodex.concrete.amqp.AMQPConnectionFacade;
import org.coodex.concrete.amqp.AMQPModule;
import org.coodex.concrete.common.*;
import org.coodex.concrete.message.ServerSideMessage;
import org.coodex.concrete.own.OwnServiceModule;
import org.coodex.concrete.own.OwnServiceProvider;
import org.coodex.concrete.own.RequestPackage;
import org.coodex.concrete.own.ResponsePackage;
import org.coodex.util.GenericType;

import java.io.IOException;
import java.util.Map;

import static org.coodex.concrete.amqp.AMQPConstants.ROUTE_KEY_REQUEST;
import static org.coodex.concrete.amqp.AMQPConstants.ROUTE_KEY_RESPONSE;
import static org.coodex.concrete.amqp.AMQPHelper.getExchangeName;

public class AMQPApplication extends OwnServiceProvider {

    private static OwnModuleBuilder AMQP_MODULE_BUILDER = new OwnModuleBuilder() {
        @Override
        public OwnServiceModule build(Class clz) {
            return new AMQPModule(clz);
        }
    };
    private final String exchangeName;
    private Channel channel;


    public AMQPApplication(AMQPConnectionConfig config){
        this(config, null);
    }
    /**
     *
     * @param config
     * @param exchangeName 服务绑定到哪个交换机上
     */
    public AMQPApplication(AMQPConnectionConfig config, String exchangeName) {
        this.exchangeName = getExchangeName(exchangeName);
        connect(config);
    }

    @Override
    protected OwnModuleBuilder getModuleBuilder() {
        return AMQP_MODULE_BUILDER;
    }

    @Override
    protected Subjoin getSubjoin(Map<String, String> map) {
        return new AMQPSubjoin(map);
    }

    @Override
    protected ServerSideContext getServerSideContext(RequestPackage<Object> requestPackage,
                                                     String tokenId, Caller caller) {
        Subjoin subjoin = getSubjoin(requestPackage);
        return new AMQPServiceContext(caller, subjoin, getLocale(subjoin), tokenId);
    }

    @Override
    protected String getModuleName() {
        return "amqp";
    }

    private void connect(AMQPConnectionConfig config) {
        try {
            final JSONSerializer serializer = JSONSerializerFactory.getInstance();
            if (serializer == null) throw new RuntimeException("none json serializer found.");

            Connection connection = AMQPConnectionFacade.getConnection(config);
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);
            String queueName = channel.queueDeclare().getQueue();
            // request data use routingKey: request.clientId
            channel.queueBind(queueName, exchangeName, ROUTE_KEY_REQUEST + "*");
            channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
                private void send(String json, String clientId) throws IOException {
                    synchronized (channel) {
                        channel.basicPublish(exchangeName, ROUTE_KEY_RESPONSE + clientId, null,
                                json.getBytes("UTF-8"));
                    }
                }


                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    final String clientId = envelope.getRoutingKey().substring(ROUTE_KEY_REQUEST.length());
                    final RequestPackage<Object> requestPackage = serializer.parse(new String(body, "UTF-8"),
                            new GenericType<RequestPackage<Object>>() {
                            }.genericType());

                    invokeService(requestPackage, new AMQPCaller(getSubjoin(requestPackage)),
                            new DefaultResponseVisitor() {
                                @Override
                                public void visit(String json) {
                                    try {
                                        send(json, clientId);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }, new ErrorVisitor() {
                                @Override
                                public void visit(String msgId, Throwable th) {
                                    ResponsePackage<ErrorInfo> responsePackage = new ResponsePackage<ErrorInfo>();
                                    responsePackage.setOk(false);
                                    responsePackage.setMsgId(msgId);
                                    responsePackage.setContent(ThrowableMapperFacade.toErrorInfo(th));
                                    try {
                                        send(serializer.toJson(requestPackage), clientId);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }, new ServerSideMessageVisitor() {
                                @Override
                                public void visit(ServerSideMessage serverSideMessage, String tokenId) {
                                    // TODO
                                }
                            }, null);
                }
            });
        } catch (Throwable th) {
            throw ConcreteHelper.findException(th);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (channel != null && channel.isOpen()) {
            channel.close();
            channel = null;
        }
    }
}
