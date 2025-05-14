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

package org.coodex.billing.timebased;


import org.coodex.billing.Chargeable;

public interface TimeBasedChargeable extends Chargeable {

    /**
     * @return 消费的原始记录，保证同一笔原始记录在任意时刻计费都是一致的
     */
    String getRefId();

    /**
     * @return 计费时段
     */
    Period getPeriod();

    void setPeriod(Period period);

    /**
     * @return 模型名称
     */
    String getModel();

    void setModel(String model);

    /**
     * @return 模型参数
     */
    String getModelParam();

    void setModelParam(String modelParam);
}
