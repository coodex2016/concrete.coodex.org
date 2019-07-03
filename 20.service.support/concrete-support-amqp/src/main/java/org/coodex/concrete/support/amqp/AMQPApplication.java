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
import org.coodex.util.Common;
import org.coodex.util.GenericType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.coodex.concrete.amqp.AMQPConstants.ROUTE_KEY_REQUEST;
import static org.coodex.concrete.amqp.AMQPConstants.ROUTE_KEY_RESPONSE;
import static org.coodex.concrete.amqp.AMQPHelper.getExchangeName;

public class AMQPApplication extends OwnServiceProvider {

    private final static Logger log = LoggerFactory.getLogger(AMQPApplication.class);


    private static OwnModuleBuilder AMQP_MODULE_BUILDER = new OwnModuleBuilder() {
        @Override
        public OwnServiceModule build(Class clz) {
            return new AMQPModule(clz);
        }
    };
    private final String exchangeName;
    private Channel channel;


    public AMQPApplication(AMQPConnectionConfig config) {
        this(config, null, null, null);
    }

    /**
     *
     * @param config 连接信息
     * @param exchangeName 交换机名称，如果为空，则使用默认交换机名
     * @param queueName 队列名称，如果为空，则使用auto-delete队列，否则为持久化队列
     * @param ttl 队列消息ttl，为空则不设置
     */
    public AMQPApplication(AMQPConnectionConfig config, String exchangeName, String queueName, Long ttl) {
        this.exchangeName = getExchangeName(exchangeName);
        connect(config, queueName, ttl);
    }

    @Override
    protected OwnModuleBuilder getModuleBuilder() {
        return AMQP_MODULE_BUILDER;
    }

    @Override
    protected Subjoin getSubjoin(Map<String, String> map) {
        return new AMQPSubjoin(map).wrap();
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

    private Map<String,Object> getQueueArgumenets(Long ttl){
        if(ttl != null && ttl > 0){
            Map<String, Object> args = new HashMap<>();
            args.put("x-message-ttl", 60000);
            return args;
        } else {
            return null;
        }
    }

    private void connect(AMQPConnectionConfig config, String queueName, Long ttl) {
        try {
            final JSONSerializer serializer = JSONSerializerFactory.getInstance();
            if (serializer == null) throw new RuntimeException("none json serializer found.");

            Connection connection = AMQPConnectionFacade.getConnection(config);
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);
            String queue = queueName;
            if( Common.isBlank(queue) ){
                queue =channel.queueDeclare().getQueue();
            }  else {
                channel.queueDeclare(queue,true,false,false,getQueueArgumenets(ttl));
            }
            // request data use routingKey: request.clientId
            channel.queueBind(queue, exchangeName, ROUTE_KEY_REQUEST + "*");
            channel.basicConsume(queue, true, new DefaultConsumer(channel) {
                private void send(String json, String clientId) throws IOException {
                    log.debug("send to {}: {}", clientId, json);
                    channel.basicPublish(exchangeName,
                            ROUTE_KEY_RESPONSE + clientId,
                            null,
                            json.getBytes(StandardCharsets.UTF_8));
                }


                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    final String clientId = envelope.getRoutingKey().substring(ROUTE_KEY_REQUEST.length());
                    String bodyStr = new String(body, StandardCharsets.UTF_8);
                    log.debug("message received: {}", bodyStr);
                    final RequestPackage<Object> requestPackage = serializer.parse(bodyStr,
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
                                        send(serializer.toJson(responsePackage), clientId);
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
