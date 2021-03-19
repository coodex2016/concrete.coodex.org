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

import java.math.BigDecimal;

/**
 * 消费品
 */
public class ConsumerGoods implements Adjustment<Chargeable> {
    private final int price;
    private final float quantity;
    private final String name;

    /**
     * @param price    单价
     * @param quantity 数量
     * @param name     名称
     */
    public ConsumerGoods(int price, float quantity, String name) {
        this.price = price;
        this.quantity = quantity;
        this.name = name;
    }

    @Override
    public long adjust(Bill<Chargeable> bill) {
        return BigDecimal.valueOf(price).multiply(
                BigDecimal.valueOf(quantity)
        ).setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
    }

    @Override
    public String getName() {
        return name + "[" + price + " x " + quantity + "]";
    }

    @SuppressWarnings("unused")
    public int getPrice() {
        return price;
    }

    @SuppressWarnings("unused")
    public float getQuantity() {
        return quantity;
    }
}
