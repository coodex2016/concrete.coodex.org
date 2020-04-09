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

package org.coodex.concrete.message;

import org.coodex.concrete.message.serializers.DefaultSerializer;
import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.SelectableServiceLoader;

import java.io.Serializable;
import java.lang.reflect.Type;

import static org.coodex.concrete.message.TopicBuilder.buildTopic;
import static org.coodex.util.Common.cast;

public class Topics {

    public static final String TAG_QUEUE = "queue";
    public static final String QUEUE_USERNAME = "username";
    public static final String QUEUE_PA55W0RD = "password";
    public static final String SERIALIZER_TYPE = "serializer";
    private static final Serializer DEFAULT_SERIALIZER = new DefaultSerializer();
    private static SelectableServiceLoader<String, Serializer> serializerSelectableServiceLoader =
            new LazySelectableServiceLoader<String, Serializer>(DEFAULT_SERIALIZER) {
            };

//    @SuppressWarnings("rawtypes")
//    private static SelectableServiceLoader<Class<? extends AbstractTopic>, TopicPrototypeProvider> topicProviders =
//            new LazySelectableServiceLoader<Class<? extends AbstractTopic>, TopicPrototypeProvider>() {
//            };

    public static Serializer getSerializer(String serializerType) {
        return serializerSelectableServiceLoader.select(serializerType);
//        Serializer serializer = serializerSelectableServiceLoader.select(serializerType);
//        return serializer == null ? DEFAULT_SERIALIZER : serializer;
    }

//    public static <M, T extends AbstractTopic<M>> T get(GenericType<T> genericType) {
//        return get(genericType, (Class<?>) null);
//    }

//    public static <M, T extends AbstractTopic<M>> T get(GenericType<T> genericType, Class<?> context) {
//        return Topics.<M, T>get(genericType.genericType(context));
//    }

    public static <M extends Serializable, T extends AbstractTopic<M>> T get(Type type) {
        return Topics.get(type, null);
    }

//    public static <M, T extends AbstractTopic<M>> T get(GenericType<T> genericType, String queue) {
//        return get(genericType, null, queue);
//    }

//    public static <M, T extends AbstractTopic<M>> T get(GenericType<T> genericType, Class<?> context, String queue) {
//        return Topics.<M, T>get(genericType.genericType(context), queue);
//    }

    public static <M extends Serializable, T extends AbstractTopic<M>> T get(Type type, String queue) {
        return cast(buildTopic(new TopicKey(queue, type)));
    }


}
