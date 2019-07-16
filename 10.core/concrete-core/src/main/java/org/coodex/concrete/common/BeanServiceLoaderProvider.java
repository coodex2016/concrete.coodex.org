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

import org.coodex.util.ReflectHelper;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public class BeanServiceLoaderProvider extends ServiceLoaderImpl<BeanProvider> {

    private static final BeanProvider DEFAULT_PROVIDER = ReflectHelper.throwExceptionObject(
            BeanProvider.class, new ConcreteException(ErrorCodes.NO_BEAN_PROVIDER_FOUND));
    private static final ServiceLoader<BeanProvider> SPI_INSTANCE = new BeanServiceLoaderProvider();

    public static BeanProvider getBeanProvider() {
        return SPI_INSTANCE.getInstance();
    }

    @Override
    public BeanProvider getDefaultProvider() {
        return DEFAULT_PROVIDER;
    }
}
