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

package org.coodex.billing.box;

import org.coodex.billing.Adjustment;
import org.coodex.billing.Bill;
import org.coodex.billing.Chargeable;

public abstract class AbstractOffAdjustment<C extends Chargeable> implements Adjustment<C> {
    private final float offRate;
    private final String name;

    protected AbstractOffAdjustment(float offRate, String name) {
        this.offRate = offRate;
        this.name = name;
    }

    @SuppressWarnings("unused")
    public float getOffRate() {
        return offRate;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @param bill 账单
     * @return 需要打折的总数
     */
    protected abstract long total(Bill<C> bill);

    @Override
    public long adjust(Bill<C> bill) {
        return (long) (-total(bill) * offRate);
    }
}
