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

import org.coodex.concrete.client.Destination;

import java.util.Objects;

public class AMQPDestination extends Destination {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String virtualHost;
    private String exchangeName;
    private String sharedExecutorName;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getSharedExecutorName() {
        return sharedExecutorName;
    }

    public void setSharedExecutorName(String sharedExecutorName) {
        this.sharedExecutorName = sharedExecutorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AMQPDestination)) return false;
        if (!super.equals(o)) return false;

        AMQPDestination that = (AMQPDestination) o;

        if (!Objects.equals(host, that.host)) return false;
        if (!Objects.equals(port, that.port)) return false;
        if (!Objects.equals(username, that.username)) return false;
        if (!Objects.equals(password, that.password)) return false;
        if (!Objects.equals(virtualHost, that.virtualHost)) return false;
        return Objects.equals(exchangeName, that.exchangeName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (virtualHost != null ? virtualHost.hashCode() : 0);
        result = 31 * result + (exchangeName != null ? exchangeName.hashCode() : 0);
        return result;
    }
}
