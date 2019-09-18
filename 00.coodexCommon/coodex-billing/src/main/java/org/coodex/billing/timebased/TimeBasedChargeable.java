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
     * @return 计费时段
     */
    Period getPeriod();

    /**
     * @return 模型名称
     */
    String getModel();

    /**
     * @return 模型参数
     */
    String getModelParam();
}
