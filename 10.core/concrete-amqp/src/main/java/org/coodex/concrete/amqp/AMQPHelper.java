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

package org.coodex.concrete.amqp;

import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.IF;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.coodex.concrete.amqp.AMQPConstants.DEFAULT_EXCHANGE_NAME;

public class AMQPHelper {

    private static SingletonMap<Class, AMQPModule> modules =
            new SingletonMap<>(
                    AMQPModule::new
            );


    @SuppressWarnings("unchecked")
    public static AMQPUnit findUnit(DefinitionContext context) {
        AMQPModule module = IF.isNull(modules.get(context.getDeclaringClass()),
                context.getDeclaringClass() + "is not a concrete service.");
        Method method = context.getDeclaringMethod();
        for (AMQPUnit unit : module.getUnits()) {
            if (unit.getMethod().getName().equals(method.getName()) &&
                    Arrays.equals(unit.getMethod().getParameterTypes(), method.getParameterTypes())) {
                return unit;
            }
        }
        throw new RuntimeException("unable found amqp unit: "
                + context.getDeclaringClass().getName() + " "
                + context.getDeclaringMethod().getName());
    }


    public static String getExchangeName(String exchangeName){
        return Common.isBlank(exchangeName) ? DEFAULT_EXCHANGE_NAME : exchangeName;
    }
}
