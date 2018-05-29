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

import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.count.CounterFacade;
import org.coodex.util.Common;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.inject.Inject;

/**
 * Created by davidoff shen on 2017-04-18.
 */
public class Starter {

    @Inject
    private BoxedCounter counter;

    public static void main(String[] args) throws InterruptedException {

        new ClassPathXmlApplicationContext("counter.xml");

        for (int i = 0; i < 10000; i++) {
            CounterFacade.count(new Pojo2(Common.random(0, 2000)));
        }

        ExecutorsHelper.shutdownAll();

    }

    public int[] getBoxedCount() {
        return counter.getBoxes();
    }

}
