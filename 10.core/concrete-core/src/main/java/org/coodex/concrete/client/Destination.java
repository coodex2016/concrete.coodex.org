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

package org.coodex.concrete.client;

import java.io.Serializable;

public abstract class Destination implements Serializable {
    public static final Integer DEFAULT_REQUEST_TIMEOUT = 5000;
    private String identify;
    private String location;
    private String tokenManagerKey;
    private Integer timeout;
    private boolean tokenTransfer;


    private boolean async;

    public String getIdentify() {
        return identify;
    }

    public void setIdentify(String identify) {
        this.identify = identify;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTokenManagerKey() {
        return tokenManagerKey;
    }

    public void setTokenManagerKey(String tokenManagerKey) {
        this.tokenManagerKey = tokenManagerKey;
    }

    public boolean isTokenTransfer() {
        return tokenTransfer;
    }

    public void setTokenTransfer(boolean tokenTransfer) {
        this.tokenTransfer = tokenTransfer;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public Integer getTimeout() {
        return timeout == null || timeout <= 0 ? DEFAULT_REQUEST_TIMEOUT : timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Destination)) return false;

        Destination that = (Destination) o;

        if (tokenTransfer != that.tokenTransfer) return false;
        if (async != that.async) return false;
        if (identify != null ? !identify.equals(that.identify) : that.identify != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        if (tokenManagerKey != null ? !tokenManagerKey.equals(that.tokenManagerKey) : that.tokenManagerKey != null)
            return false;
        return timeout != null ? timeout.equals(that.timeout) : that.timeout == null;
    }

    @Override
    public int hashCode() {
        int result = identify != null ? identify.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (tokenManagerKey != null ? tokenManagerKey.hashCode() : 0);
        result = 31 * result + (timeout != null ? timeout.hashCode() : 0);
        result = 31 * result + (tokenTransfer ? 1 : 0);
        result = 31 * result + (async ? 1 : 0);
        return result;
    }

}
