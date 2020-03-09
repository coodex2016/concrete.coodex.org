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

import org.coodex.billing.timebased.reference.*;
import org.coodex.billing.timebased.reference.box.FragmentSlicerProfile;

import java.util.Arrays;
import java.util.List;

import static org.coodex.billing.demo.Constants.MODEL_01;

public class Model01ProfileFactory implements ModelProfileFactory {

    private static FragmentProfile getFragmentProfile(final int large, final int middle, final int small, final String start, final String end) {
        return new FragmentProfile() {
            @Override
            public AlgorithmProfile getAlgorithmProfile() {
                return new PerHourAlgorithmProfile(large, middle, small);
            }

            @Override
            public SlicerProfile getSlicerProfile() {
                return new FragmentSlicerProfile(start, end);
            }
        };
    }

    @Override
    public ModelProfile build(String s) {
        return new ModelProfile() {
            @Override
            public AlgorithmProfile getWholeTimeAlgorithmProfile() {
                return null;
            }

            @Override
            public List<FragmentProfile> getFragmentProfiles() {
                return Arrays.asList(
                        getFragmentProfile(228,128,88,"02:00","08:00"),
                        getFragmentProfile(118,68,48,"08:00","18:00"),
                        getFragmentProfile(448,248,168,"18:00","02:00")
                );
            }
        };
    }

    @Override
    public boolean accept(String param) {
        return param != null && param.equals(MODEL_01);
    }
}
