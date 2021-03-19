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

import org.coodex.billing.Bill;
import org.coodex.billing.BillCalculator;
import org.coodex.billing.Revision;
import org.coodex.billing.box.AmountAdjustment;
import org.coodex.billing.box.ConsumerGoods;
import org.coodex.billing.box.ZeroRemainderAdjustment;
import org.coodex.billing.timebased.Period;
import org.coodex.billing.timebased.TimeBasedChargeable;
import org.coodex.billing.timebased.TimeBasedDetail;
import org.coodex.billing.timebased.reference.box.DurationRevision;
import org.coodex.billing.timebased.reference.box.TimeBasedOffAdjustment;
import org.coodex.util.Common;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.coodex.billing.demo.Constants.MODEL_01;

public class KTVConsumption implements TimeBasedChargeable {

    public static void main(String[] args) {
        KTVConsumption ktvConsumption = new KTVConsumption();
        Bill<KTVConsumption> bill = BillCalculator.calc(ktvConsumption);
        StringBuilder builder = new StringBuilder("`月月鸟`在`KTV`的消费总金额 ")
                .append(bill.getAmount()).append(" 元。明细如下：");
        for (Bill.Detail detail : bill.getDetails()) {
            builder.append("\n  ");
            if (detail instanceof TimeBasedDetail) {
                TimeBasedDetail timeBasedDetail = (TimeBasedDetail) detail;
                builder.append("[")
                        .append(Common.calendarToStr(timeBasedDetail.getPeriod().getStart()))
                        .append(" - ")
                        .append(Common.calendarToStr(timeBasedDetail.getPeriod().getEnd()))
                        .append("]");
            }
            builder.append(detail.item()).append(" 金额: ").append(detail.getAmount()).append("元");
            if (detail.usedRevision() != null) {
                builder.append(" ").append(detail.usedRevision().getName());
            }
        }
        System.out.println(builder.toString());
    }

    @Override
    public Period getPeriod() {
        try {
            return Period.BUILDER.create(
                    Common.strToCalendar("2019-09-18 16:47:30", Common.DEFAULT_DATETIME_FORMAT),
                    Common.strToCalendar("2019-09-21 20:50:00", Common.DEFAULT_DATETIME_FORMAT)
            );
        } catch (ParseException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public String getModel() {
        return MODEL_01;
    }

    @Override
    public String getModelParam() {
        return MODEL_01 + "_01";
    }

    @Override
    public void setModelParam(String modelParam) {
        // 多条规则时使用
    }

    @Override
    public void setModel(String model) {
        // 多条规则时使用
    }

    @Override
    public void setPeriod(Period period) {
        // 多条规则时使用
    }

    @Override
    public String getRefId() {
        return "月月鸟";
    }

    @Override
    public List<Revision> getRevisions() {
        return Arrays.asList(
                new DurationRevision("2小时抵扣", 120),
                new TimeBasedOffAdjustment<KTVConsumption>(0.4f, "6折房间费"),
                new AmountAdjustment(1500, "1500元代金券"),
                new ConsumerGoods(10,10,"方便面"),
                new ConsumerGoods(15,7,"XXX"),
                new ZeroRemainderAdjustment("抹零",100)
        );
    }

    public Room getRoomType() {
        return Room.MIDDLE;
    }

    public enum Room {
        LARGE, MIDDLE, SMALL
    }
}
