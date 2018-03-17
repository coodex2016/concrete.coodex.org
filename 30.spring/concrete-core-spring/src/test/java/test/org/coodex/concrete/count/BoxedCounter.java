/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.spring.SpecificMomentSegmentation;
import org.coodex.count.Segmentation;
import org.coodex.count.SegmentedCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Arrays;

/**
 * Created by davidoff shen on 2017-04-18.
 */
@Named
public class BoxedCounter implements SegmentedCounter<Pojo> {

    private final static Logger log = LoggerFactory.getLogger(BoxedCounter.class);


    private static final int boxCount = 15;
    private static final int boxSize = 100;
    private int[] boxes = new int[boxCount + 1];

    public BoxedCounter() {
        Arrays.fill(boxes, 0);
    }


    @Override
    public void count(Pojo value) {
        int boxNo = value.getValue() / boxSize;
        boxes[boxNo >= boxCount ? boxCount : boxNo]++;
        log.debug("boxes: {}", boxes);
    }

    public int[] getBoxes() {
        return boxes;
    }

    @Override
    public Segmentation getSegmentation() {
        return new SpecificMomentSegmentation("0/3 * * * * *");
    }

    @Override
    public void slice() {
        log.info("slice ...");
        Arrays.fill(boxes, 0);
    }
}
