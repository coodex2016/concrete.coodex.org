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

package org.coodex.concrete.common;

import org.coodex.util.Singleton;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public class BeanServiceLoaderProvider /*extends ServiceLoaderImpl<BeanProvider>*/ {

////    private static final BeanProvider DEFAULT_PROVIDER =
//
//    private static final Singleton<BeanProvider> DEFAULT_PROVIDER_SINGLETON = new Singleton<>(
//            () -> ReflectHelper.throwExceptionObject(
//                    BeanProvider.class, new ConcreteException(ErrorCodes.NO_BEAN_PROVIDER_FOUND))
//    );
//
//    private static final ServiceLoader<BeanProvider> SPI_INSTANCE = new ServiceLoaderImpl<BeanProvider>() {
//    };//new BeanServiceLoaderProvider();

    private static Singleton<BeanProvider> beanProviderSingleton = Singleton.with(
            () -> {
                ServiceLoader<BeanProvider> serviceLoader = ServiceLoader.load(BeanProvider.class);
                Iterator<BeanProvider> iterable = serviceLoader.iterator();
                return iterable.hasNext() ? iterable.next() : new BeanProvider() {
                    @Override
                    public <T> T getBean(Class<T> type) {
                        return null;
                    }

                    @Override
                    public <T> Map<String, T> getBeansOfType(Class<T> type) {
                        return new HashMap<>();
                    }
                };
            }
    );

    public static BeanProvider getBeanProvider() {
//        return SPI_INSTANCE.getInstance();
        return beanProviderSingleton.get();
    }

//    @Override
//    public BeanProvider getDefaultProvider() {
//        return DEFAULT_PROVIDER_SINGLETON.getInstance();
//    }
}
