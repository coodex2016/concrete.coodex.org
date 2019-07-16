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

package org.coodex.concrete.prod.interceptors;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.intercept.AbstractSyncInterceptor;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.prod.Modules;
import org.coodex.concrete.prod.ProductFactory;
import org.coodex.concrete.prod.impl.DefaultProductFactory;
import org.coodex.util.ServiceLoaderImpl;
import org.coodex.util.Singleton;

import java.util.Set;

import static org.coodex.concrete.core.intercept.InterceptOrders.OTHER;
import static org.coodex.concrete.prod.Product.PRODUCTION_KEY;
import static org.coodex.concrete.prod.interceptors.ProductErrorCodes.INVALID_KEY;
import static org.coodex.concrete.prod.interceptors.ProductErrorCodes.NONE_KEY;

@ServerSide
public class ProductInterceptor extends AbstractSyncInterceptor {

    private Singleton<ProductFactory> productFactorySingleton = new Singleton<>(
            () -> new ServiceLoaderImpl<ProductFactory>(new DefaultProductFactory()) {
            }.getInstance()
    );

    public ProductInterceptor() {
        //noinspection unchecked
        ErrorMessageFacade.register(ProductErrorCodes.class);
    }

    @Override
    protected boolean accept_(DefinitionContext context) {
        return context != null && context.getAnnotation(Modules.class) != null;
    }

    @Override
    public void before(DefinitionContext context, MethodInvocation joinPoint) {
        Modules modules = context.getAnnotation(Modules.class);
        if (modules == null) {
            return;
        }
        Subjoin subjoin = SubjoinWrapper.getInstance();
        String key = subjoin.get(PRODUCTION_KEY);
        Set<Warning> warnings = IF.isNull(
                productFactorySingleton.getInstance().getProductInstance(
                        IF.isNull(key, NONE_KEY)
                ), INVALID_KEY, key)
                .check(modules.values());

        if (warnings != null && warnings.size() > 0) {
            warnings.forEach(subjoin::putWarning);
        }
    }

    @Override
    public int getOrder() {
        return OTHER;
    }
}
