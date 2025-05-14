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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.License;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;

import static org.coodex.concrete.common.ErrorCodes.ABOUT_LICENSE;
import static org.coodex.concrete.core.intercept.InterceptOrders.LICENSE_CHECK;

/**
 * license 检查切片，需要实现{@link License}，并放到SPI
 */
@ServerSide
public class LicenseCheckInterceptor extends AbstractSyncInterceptor {

    private final ServiceLoader<License> licenseServiceLoader = new LazyServiceLoader<License>((License) () -> null) {
    };
    private final Subjoin subjoin = SubjoinWrapper.getInstance();

    @Override
    protected boolean accept_(DefinitionContext context) {
        return true;
    }

    @Override
    public int getOrder() {
        return LICENSE_CHECK;
    }


    @Override
    public void before(DefinitionContext context, MethodInvocation joinPoint) {
        try {
            String message = licenseServiceLoader.get().check();
            if (message != null) {
                subjoin.putWarning(new WarningData(ABOUT_LICENSE, message));
            }
        } catch (License.OverdueException e) {
            throw new ConcreteException(ABOUT_LICENSE, e.getLocalizedMessage());
        }
    }
}
