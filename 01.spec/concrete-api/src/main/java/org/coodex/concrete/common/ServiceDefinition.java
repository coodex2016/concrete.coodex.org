/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.common;

import org.coodex.concrete.api.ConcreteService;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by davidoff shen on 2016-09-09.
 */
public class ServiceDefinition {


    private Class<? extends ConcreteService> serviceClass;

    private Set<Method> methods = new HashSet<Method>();

    ServiceDefinition(Class<? extends ConcreteService> serviceClass, Collection<Method> methods) {
        this.serviceClass = serviceClass;
        this.methods.addAll(methods);
    }

    public Class<? extends ConcreteService> getServiceClass() {
        return serviceClass;
    }

    public Set<Method> getMethods() {
        return methods;
    }
}
