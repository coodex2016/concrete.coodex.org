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

package org.coodex.concrete.jaxrs.saas;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.ConcreteClosure;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ReverseProxyErrorCodes;
import org.coodex.concrete.jaxrs.Client;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.concrete.support.jsr339.AbstractJSR339Resource;
import org.coodex.concurrent.components.PriorityRunnable;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2017-03-22.
 */
public class ReverseResource<T extends ConcreteService> extends AbstractJSR339Resource<T> {

    @Context
    private HttpHeaders httpHeaders;

    @Override
    protected void __execute(String methodName, final AsyncResponse asyncResponse, final String tokenId, final Object... params) {
        final Method method = findMethod(methodName, null);
        final DeliveryContext deliveryContext = new DeliveryContext(asyncResponse,
                new MultivaluedHashMap<String, String>(httpHeaders.getRequestHeaders()));

        getExecutor().execute(new PriorityRunnable(getPriority(method), new Runnable() {
            @Override
            public void run() {
                try {

                    Unit unit = JaxRSHelper.getUnitFromContext(ConcreteHelper.getContext(method, getInterfaceClass())/*, params*/);

                    String routeBy = Assert.isNull(unit.getAnnotation(RouteBy.class),
                            //.getDeclaringModule().getDeclaredAnnotation(RouteBy.class),
                            ReverseProxyErrorCodes.ROUTE_BY_NOT_FOUND,
                            unit.getDeclaringModule().getInterfaceClass()).value();
                    Reverser reverser = ReverserFactory.getReverser(routeBy);

                    // get domain from method
                    String domain = null;
                    boolean found = false;
                    for (int i = 0; i < unit.getParameters().length; i++) {
                        Param param = unit.getParameters()[i];
                        if (param.getName().equals(routeBy)) {
                            domain = reverser.resolve((String) params[i]);
                            found = true;
                            break;
                        }
                    }
                    final String sDomain = domain;
                    Assert.not(found, ReverseProxyErrorCodes.ROUT_BY_PARAMETER_NOT_FOUND, unit, routeBy);

                    // closure
                    DeliveryContext.closureRun(deliveryContext, new ConcreteClosure() {
                        @Override
                        public Object concreteRun() throws Throwable {
                            ConcreteService client = sDomain == null ?
                                    Client.getInstance(getInterfaceClass()) :
                                    Client.getInstance(getInterfaceClass(), sDomain);

                            method.invoke(client, params);
                            return null;
                        }
                    });


//                    asyncResponse.resume(invokeByTokenId(tokenId, method, params));
                } catch (Throwable th) {
                    asyncResponse.resume(th);
                }
            }
        }));
    }
}
