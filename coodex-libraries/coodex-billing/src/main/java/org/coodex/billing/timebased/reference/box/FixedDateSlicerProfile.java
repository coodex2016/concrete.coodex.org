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

package org.coodex.billing.timebased.reference.box;

import org.coodex.billing.timebased.reference.SlicerProfile;

/**
 * 按自然日切片的参数
 */
public class FixedDateSlicerProfile implements SlicerProfile {
    private final String startTime;

    @SuppressWarnings("unused")
    public FixedDateSlicerProfile() {
        this(null);
    }

    /**
     * @param startTime 从一天的哪个时刻开始，默认为0点
     */
    public FixedDateSlicerProfile(String startTime) {
        this.startTime = startTime == null ? "00:00" : startTime;
    }

    public String getStartTime() {
        return startTime;
    }
}
