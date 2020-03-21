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

package org.coodex.billing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 结算单信息
 */
public class Bill<C extends Chargeable> {
    private final C chargeable;
    private List<Detail> details = new ArrayList<>();

    public Bill(C chargeable) {
        this.chargeable = chargeable;
    }

    /**
     * @return 结算单的计费领域对象
     */
    public C getChargeable() {
        return chargeable;
    }

    /**
     * @return 全部明细
     */
    public List<Detail> getDetails() {
        return details;
    }

    /**
     * 添加一条明细
     *
     * @param detail 账单明细
     */
    public void addDetail(Detail detail) {
        details.add(detail);
    }

    /**
     * @param details 添加一批明细
     */
    public void addAllDetails(List<Detail> details) {
        this.details.addAll(details);
    }

    /**
     * @return 账单总金额
     */
    public long getAmount() {
        long amount = 0;
        for (Detail detail : details) {
            amount += detail.getAmount();
        }
        return amount <= 0 ? 0 : amount;
    }

    /**
     * @return 所有被用掉的调整项目
     */
    @SuppressWarnings("unused")
    public Set<Revision> getUsedRevisions() {
        Set<Revision> revisionSet = new HashSet<>();
        for (Detail detail : details) {
            if (detail.usedRevision() != null)
                revisionSet.add(detail.usedRevision());

        }
        return revisionSet;
    }

    /**
     * 结算明细
     */
    public interface Detail {
        /**
         * @return 金额
         */
        long getAmount();

        /**
         * @return 条目名称
         */
        String item();

        /**
         * @return 本条明细所使用的调整项目
         */
        Revision usedRevision();
    }

    public static class AdjustDetail implements Detail {
        private final long amount;
        private final String item;
        private final Revision revision;

        public AdjustDetail(long amount, String item, Revision revision) {
            this.amount = amount;
            this.item = item;
            this.revision = revision;
        }

        @Override
        public long getAmount() {
            return amount;
        }

        @Override
        public String item() {
            return item;
        }

        @Override
        public Revision usedRevision() {
            return revision;
        }
    }

}
