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

package org.coodex.billing.demo;

import org.coodex.billing.timebased.reference.AlgorithmProfile;

public class PerHourAlgorithmProfile implements AlgorithmProfile {
    private int priceLargeRoom;
    private int priceMiddleRoom;
    private int priceSmallRoom;

    public PerHourAlgorithmProfile(int priceLargeRoom, int priceMiddleRoom, int priceSmallRoom) {
        this.priceLargeRoom = priceLargeRoom;
        this.priceMiddleRoom = priceMiddleRoom;
        this.priceSmallRoom = priceSmallRoom;
    }

    public int getPriceLargeRoom() {
        return priceLargeRoom;
    }

    public int getPriceMiddleRoom() {
        return priceMiddleRoom;
    }

    public int getPriceSmallRoom() {
        return priceSmallRoom;
    }
}
