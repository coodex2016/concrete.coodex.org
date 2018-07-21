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

package org.coodex.concrete.client.impl;

import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.Invoker;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.util.Common;
import org.coodex.util.Profile;

import java.lang.reflect.Method;

import static org.coodex.concrete.common.ConcreteHelper.isDevModel;


public abstract class AbstractInvoker implements Invoker {

    private final static Profile devModeProfile = ConcreteHelper.getProfile("moduleMock");
    private final Destination destination;

    public AbstractInvoker(Destination destination) {
        this.destination = destination;
    }

    public abstract ServiceContext buildContext(final Class concreteClass, final Method method);

    protected boolean isMock() {
        String key = Common.isBlank(destination.getIdentify()) ? "client" : ("client." + destination.getIdentify());
        return devModeProfile.getBool(key, false) || isDevModel(key);
    }

    protected Destination getDestination() {
        return destination;
    }


}
