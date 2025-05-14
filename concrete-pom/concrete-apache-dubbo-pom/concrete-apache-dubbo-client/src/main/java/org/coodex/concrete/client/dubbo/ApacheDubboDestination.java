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

package org.coodex.concrete.client.dubbo;

import org.coodex.concrete.client.Destination;

import java.util.Arrays;
import java.util.Objects;

public class ApacheDubboDestination extends Destination {

    private String name;
    private String[] registries;
    private String protocol;
    private String url;


    public String[] getRegistries() {
        return registries;
    }

    public void setRegistries(String[] registries) {
        this.registries = registries;
        if (this.registries != null && this.registries.length > 1) {
            Arrays.sort(this.registries);
        }
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApacheDubboDestination)) return false;
        if (!super.equals(o)) return false;

        ApacheDubboDestination that = (ApacheDubboDestination) o;

        if (!Objects.equals(name, that.name)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(registries, that.registries)) return false;
        if (!Objects.equals(protocol, that.protocol)) return false;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(registries);
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
