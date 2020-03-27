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

package org.coodex.concrete.spring.boot;

import org.coodex.concrete.amqp.AMQPConnectionConfig;
import org.coodex.concrete.spring.AbstractRuntimeParameter;
import org.coodex.util.Common;
import org.springframework.core.annotation.AnnotationAttributes;

public class AMQPRuntime extends AbstractRuntimeParameter {

    private final AMQPConnectionConfig config;
    private String exchangeName;
    private String queueName;
    private long ttl;

    public AMQPRuntime() {
        this(new AMQPConnectionConfig(), null, null, null, null, -1L);
    }

    public AMQPRuntime(AMQPConnectionConfig config,
                       String[] servicePackages,
                       Class[] classes,
                       String exchangeName, String queueName, long ttl) {
        super(servicePackages, classes);
        this.config = config;
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.ttl = ttl;
        cleanConfig();
    }


    private void cleanConfig() {
        if (Common.isBlank(config.getUri()))
            config.setUri(get("location", ""));
        if (Common.isBlank(config.getHost()))
            config.setHost(get("host", ""));
        if (Common.isBlank(config.getPassword()))
            config.setPassword(get("password", ""));
        if (Common.isBlank(config.getUsername()))
            config.setUsername(get("username", ""));
        if (Common.isBlank(config.getVirtualHost()))
            config.setVirtualHost(get("virtualHost", ""));
        if (Common.isBlank(config.getSharedExecutorName()))
            config.setSharedExecutorName(get("executorName", ""));
        if (config.getPort() != null && config.getPort() <= 0)
            config.setPort(get("port", null));

    }


    public AMQPConnectionConfig getConfig() {
        return config;
    }

    public String getExchangeName() {
        return Common.isBlank(exchangeName) ?
                get("exchangeName", null) :
                exchangeName;
    }

    public String getQueueName() {
        return Common.isBlank(queueName) ?
                get("queueName", null) :
                queueName;
    }

    public long getTtl() {
        return ttl <= 0 ?
                get("ttl", 60000L) :
                ttl;
    }

    @Override
    protected String getNamespace() {
        return "amqp";
    }

    @Override
    protected void loadCustomRuntimeConfigFrom(AnnotationAttributes annotationAttributes) {
        config.setUri(annotationAttributes.getString("location"));
        config.setPort(annotationAttributes.getNumber("port"));
        config.setVirtualHost(annotationAttributes.getString("virtualHost"));
        config.setUsername(annotationAttributes.getString("username"));
        config.setPassword(annotationAttributes.getString("password"));
        config.setHost(annotationAttributes.getString("host"));
        config.setSharedExecutorName(annotationAttributes.getString("executorName"));
        this.exchangeName = annotationAttributes.getString("exchangeName");
        this.queueName = annotationAttributes.getString("queueName");
        this.ttl = annotationAttributes.getNumber("ttl");
        cleanConfig();
    }
}
