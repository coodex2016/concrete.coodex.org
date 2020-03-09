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

package org.coodex.billing.timebased.reference;

import org.coodex.billing.timebased.BillingModel;
import org.coodex.billing.timebased.TimeBasedChargeable;
import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.SelectableServiceLoader;

public abstract class AbstractBillingModel<C extends TimeBasedChargeable> implements BillingModel<C> {

    private final SelectableServiceLoader<String, ModelProfileFactory> modelProfileFactorySelectableServiceLoader
            = new LazySelectableServiceLoader<String, ModelProfileFactory>() {
    };

    /**
     * @return 计费模型编号，不可重复
     */
    protected abstract String getModelCode();

    @Override
    public boolean accept(String param) {
        return param != null && param.equals(getModelCode());
    }

    @Override
    public Instance<C> create(C chargeable) {
        return new AbstractModelInstance<C>(
                modelProfileFactorySelectableServiceLoader
                        .select(chargeable.getModel())
                        .build(chargeable.getModelParam())
        ) {
        };
    }
}
