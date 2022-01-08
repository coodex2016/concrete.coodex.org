/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.jaxrs.client;

import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;

import javax.ws.rs.client.ClientBuilder;
import java.lang.reflect.Array;

public class JaxRSClientBuilder {

    private static final ServiceLoader<ToRegisteredProvider> TO_REGISTERED_PROVIDER_SERVICE_LOADER
            = new LazyServiceLoader<ToRegisteredProvider>() {
    };

    public static ClientBuilder newClientBuilder() {
        ClientBuilder builder = ClientBuilder.newBuilder();
        TO_REGISTERED_PROVIDER_SERVICE_LOADER.getAll().forEach((k, p) -> {
            Object o = p.toRegisteredObject();
            if (o == null) return;
            if (o.getClass().isArray()) {
                for (int i = 0, l = Array.getLength(o); i < l; i++) {
                    builder.register(Array.get(o, i));
                }
            } else {
                builder.register(o);
            }
        });
        return builder;
    }
}
