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

/**
 * 抹零
 */
public class ZeroRemainderAdjustment implements Adjustment<Chargeable> {
    private final String name;
    private final int subtraction;

    /**
     * @param name        你改成
     * @param subtraction 模除的除数
     */
    public ZeroRemainderAdjustment(String name, int subtraction) {
        this.name = name;
        this.subtraction = subtraction;
    }


    @Override
    public long adjust(Bill<Chargeable> bill) {
        return - bill.getAmount() % subtraction;
    }

    @Override
    public String getName() {
        return name;
    }
}
