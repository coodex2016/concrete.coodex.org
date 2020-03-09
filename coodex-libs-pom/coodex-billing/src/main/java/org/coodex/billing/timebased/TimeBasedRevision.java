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

import org.coodex.billing.Revision;

import java.util.List;

/**
 * 基于时间的调整，可以是有状态的
 */
public interface TimeBasedRevision extends Revision {

    /**
     * @param periods 待调减的范围
     * @return 调减掉的范围，为null或空集合表示未使用，否则表示该调整在待调整范围内使用了
     */
    List<Period> revised(List<Period> periods);

}
