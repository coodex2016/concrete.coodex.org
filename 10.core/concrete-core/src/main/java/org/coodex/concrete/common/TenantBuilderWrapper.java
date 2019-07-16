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

import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;

/**
 * Created by davidoff shen on 2017-05-25.
 */
public class TenantBuilderWrapper implements TenantBuilder {

    private static final TenantBuilder defaultTenantBuilder = () -> null;

    private static final ServiceLoader<TenantBuilder> tenantBuilderConcreteServiceLoader
            = new ServiceLoaderImpl<TenantBuilder>(defaultTenantBuilder) {
    };

    private static final TenantBuilder tenantBuilder = new TenantBuilderWrapper();

    public static TenantBuilder getInstance() {
        return tenantBuilder;
    }

    @Override
    public String getTenant() {
        return tenantBuilderConcreteServiceLoader.getInstance().getTenant();
    }
}
