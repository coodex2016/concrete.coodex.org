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

    private JaxRSClientBuilder() {
    }

    private static final ServiceLoader<ToRegisteredProvider> TO_REGISTERED_PROVIDER_SERVICE_LOADER
            = new LazyServiceLoader<ToRegisteredProvider>() {
    };

    private static void register(ClientBuilder builder, Object o) {
        if (o instanceof Class) {
            builder.register((Class<?>) o);
        } else {
            builder.register(o);
        }
    }

    public static ClientBuilder newClientBuilder() {
        ClientBuilder builder = ClientBuilder.newBuilder();
        TO_REGISTERED_PROVIDER_SERVICE_LOADER.getAll().forEach((k, p) -> {
            Object o = p.toRegisteredObject();
            if (o == null) return;
            if (o.getClass().isArray()) {
                for (int i = 0, l = Array.getLength(o); i < l; i++) {
                    register(builder, Array.get(o, i));
                }
            } else {
                register(builder, o);
            }
        });
        return builder;
    }
}
