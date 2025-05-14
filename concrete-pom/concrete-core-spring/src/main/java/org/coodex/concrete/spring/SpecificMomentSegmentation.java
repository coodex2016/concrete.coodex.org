/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.spring;

import org.coodex.count.Segmentation;
import org.coodex.util.Clock;
import org.springframework.scheduling.support.CronExpression;

import java.util.Objects;

/**
 * @see org.springframework.scheduling.support.CronExpression
 * Created by davidoff shen on 2017-04-19.
 */
public class SpecificMomentSegmentation implements Segmentation {

    //    private final CronSequenceGenerator cronSequenceGenerator;
    private final CronExpression cronExpression;

    public SpecificMomentSegmentation(String cron) {
//        this.cronSequenceGenerator = new CronSequenceGenerator(cron);
        cronExpression = CronExpression.parse(cron);
    }

    @Override
    public long next() {
        return Objects.requireNonNull(cronExpression.next(Clock.now().toInstant())).toEpochMilli();
//        return cronSequenceGenerator.next(Clock.now().getTime()).getTime();
    }

}
