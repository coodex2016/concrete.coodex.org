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

package org.coodex.concrete.client.jaxrs;

import org.coodex.concrete.client.Destination;

import static org.coodex.concrete.client.jaxrs.JaxRSDestinationFactory.isSSL;

public class JaxRSDestination extends Destination {

    private String logLevel;
    private String charset;
    private String ssl;

    public boolean isSsl() {
        return isSSL(getLocation());
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getSsl() {
        return ssl;
    }

    public void setSsl(String ssl) {
        this.ssl = ssl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JaxRSDestination)) return false;
        if (!super.equals(o)) return false;

        JaxRSDestination that = (JaxRSDestination) o;

        if (logLevel != null ? !logLevel.equals(that.logLevel) : that.logLevel != null) return false;
        if (charset != null ? !charset.equals(that.charset) : that.charset != null) return false;
        return ssl != null ? ssl.equals(that.ssl) : that.ssl == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (logLevel != null ? logLevel.hashCode() : 0);
        result = 31 * result + (charset != null ? charset.hashCode() : 0);
        result = 31 * result + (ssl != null ? ssl.hashCode() : 0);
        return result;
    }



}
