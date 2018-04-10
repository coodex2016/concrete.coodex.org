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

package org.coodex.concrete;


import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.IF;

import static org.coodex.concrete.ClientHelper.*;


public class Client {

    public static <T> T getInstance(Class<T> concreteServiceClass) {
        return getInstance(concreteServiceClass, null);
    }

    public static <T> T getInstance(Class<T> concreteServiceClass, String module) {
        boolean sync = ConcreteHelper.isConcreteService(concreteServiceClass);
        IF.not(sync || isReactiveExtension(concreteServiceClass),
                concreteServiceClass + "is NOT ConcreteService.");
        Destination destination = getDestination(module);
        destination.setAsync(!sync);

        return getInstanceBuilder().build(destination, concreteServiceClass);
    }

}
