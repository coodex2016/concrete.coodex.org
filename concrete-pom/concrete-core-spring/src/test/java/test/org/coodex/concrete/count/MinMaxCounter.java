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

package test.org.coodex.concrete.count;

import org.coodex.count.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

/**
 * Created by davidoff shen on 2017-04-18.
 */
@Named
public class MinMaxCounter implements Counter<Pojo> {
    private final static Logger log = LoggerFactory.getLogger(MinMaxCounter.class);

    private int min = Integer.MAX_VALUE, max = 0;

    @Override
    public void count(Pojo value) {
        int v = value.getValue();
        if (min > v)
            min = v;
        if (max < v)
            max = v;
        log.debug("min: {}, max: {}", min, max);
    }
}
