/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

import org.coodex.util.Common;

import java.util.Calendar;

public class BillingRule implements Comparable<BillingRule> {
    private static final Calendar DEFAULT_CALENDAR = Common.longToCalendar(0);

    private Calendar start;
    private String model;
    private String modelParam;

    /**
     * @return 规则生效时刻，如不设置，则为19700101
     */
    public Calendar getStart() {
        return start == null ? DEFAULT_CALENDAR : start;
    }

    /**
     * @return 该规则的计费模型
     */
    public String getModel() {
        return model;
    }

    /**
     * @return 该规则的模型参数
     */
    public String getModelParam() {
        return modelParam;
    }

    @SuppressWarnings("unused")
    public BillingRule withModel(String model) {
        this.model = model;
        return this;
    }

    @SuppressWarnings("unused")
    public BillingRule withStart(Calendar start) {
        this.start = start;
        return this;
    }

    @SuppressWarnings("unused")
    public BillingRule withModelParam(String modelParam) {
        this.modelParam = modelParam;
        return this;
    }

    @Override
    public int compareTo(BillingRule o) {
        return getStart().compareTo(o.getStart());
    }
}
