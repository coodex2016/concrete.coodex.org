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

import org.coodex.billing.timebased.Period;
import org.coodex.billing.timebased.TimeBasedChargeable;

import java.util.List;

/**
 * 切片机
 */
public interface FragmentSlicer<C extends TimeBasedChargeable> {

    /**
     * @param period     待切时间段
     * @param chargeable 可计费对象
     * @return 从整段数据中切出当前Slicer适用的时间段
     */
    List<Period> slice(Period period, C chargeable);
}
