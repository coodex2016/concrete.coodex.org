/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.spring.components;

import java.lang.reflect.Type;
import java.util.Objects;

public class ServiceLoaderKey implements InjectInfoKey {
    private final Class<?> defaultServiceClass;
    private final Type serviceType;

    public ServiceLoaderKey(Class<?> defaultServiceClass, Type serviceType) {
        this.defaultServiceClass = defaultServiceClass;
        this.serviceType = serviceType;
    }

    public Class<?> getDefaultServiceClass() {
        return defaultServiceClass;
    }

    public Type getServiceType() {
        return serviceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceLoaderKey)) return false;

        ServiceLoaderKey that = (ServiceLoaderKey) o;

        if (!Objects.equals(defaultServiceClass, that.defaultServiceClass))
            return false;
        return Objects.equals(serviceType, that.serviceType);
    }

    @Override
    public int hashCode() {
        int result = defaultServiceClass != null ? defaultServiceClass.hashCode() : 0;
        result = 31 * result + (serviceType != null ? serviceType.hashCode() : 0);
        return result;
    }
}