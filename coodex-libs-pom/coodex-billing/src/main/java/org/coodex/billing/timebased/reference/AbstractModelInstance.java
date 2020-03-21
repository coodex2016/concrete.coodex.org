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

package org.coodex.billing.timebased.reference;

import org.coodex.billing.timebased.BillingModel;
import org.coodex.billing.timebased.Period;
import org.coodex.billing.timebased.TimeBasedChargeable;
import org.coodex.util.Common;
import org.coodex.util.LazySelectableServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractModelInstance<C extends TimeBasedChargeable> implements BillingModel.Instance<C> {
    private final static Logger log = LoggerFactory.getLogger(AbstractModelInstance.class);

    @SuppressWarnings("rawtypes")
    private static final Comparator<BillingModel.Fragment> FRAGMENT_COMPARATOR = Comparator.comparing(o -> o.getPeriod().getStart());

    private final LazySelectableServiceLoader<AlgorithmProfile, AlgorithmFactory<C, AlgorithmProfile>>
            algorithmFactorySelectableServiceLoader = new LazySelectableServiceLoader<AlgorithmProfile, AlgorithmFactory<C, AlgorithmProfile>>() {
    };

    private final LazySelectableServiceLoader<SlicerProfile, SlicerFactory<C, SlicerProfile>>
            slicerFactorySelectableServiceLoader = new LazySelectableServiceLoader<SlicerProfile, SlicerFactory<C, SlicerProfile>>() {
    };

    private final ModelProfile modelProfile;

    public AbstractModelInstance(ModelProfile modelProfile) {
        this.modelProfile = modelProfile;
    }

    private BillingModel.Algorithm<C> getAlgorithm(AlgorithmProfile algorithmProfile) {
        if (algorithmProfile == null) return null;
        AlgorithmFactory<C, AlgorithmProfile> factory = algorithmFactorySelectableServiceLoader.select(algorithmProfile);
        return factory == null ? null : factory.build(algorithmProfile);
    }

    private FragmentSlicer<C> getSlicer(SlicerProfile slicerProfile) {
        if (slicerProfile == null) return null;
        SlicerFactory<C, SlicerProfile> slicerFactory = slicerFactorySelectableServiceLoader.select(slicerProfile);
        return slicerFactory == null ? null : slicerFactory.build(slicerProfile);
    }

    @SuppressWarnings("unused")
    protected ModelProfile getModelProfile() {
        return modelProfile;
    }

    @Override
    public BillingModel.Algorithm<C> getWholeTimeAlgorithm() {
        return getAlgorithm(modelProfile.getWholeTimeAlgorithmProfile());
    }

    private void throwExceptionIfNotBlank(List<Period> periods, String label) {
        if (periods.size() > 0) {
            StringBuilder builder = new StringBuilder(label);
            for (Period p : periods) {
                builder.append("\n\tfrom ")
                        .append(Common.calendarToStr(p.getStart()))
                        .append(" to ")
                        .append(Common.calendarToStr(p.getEnd()));
            }
            throw new RuntimeException(builder.toString());
        }
    }

    @Override
    public List<BillingModel.Fragment<C>> slice(Period period, C chargeable) {
        List<BillingModel.Fragment<C>> fragments = new ArrayList<>();
        for (FragmentProfile profile : modelProfile.getFragmentProfiles()) {
            FragmentSlicer<C> slicer = getSlicer(profile.getSlicerProfile());
            if (slicer == null) {
                throw new RuntimeException("FragmentSlicer NOT found: " + profile.getSlicerProfile());
            }
            List<Period> periods = slicer.slice(period, chargeable);
            if (periods != null && periods.size() > 0) {
                throwExceptionIfNotBlank(Period.sub(periods, Collections.singletonList(period), Period.BUILDER), "out of range:");

                for (Period p : periods) {
                    fragments.add(new BillingModel.Fragment<>(getAlgorithm(profile.getAlgorithmProfile()), p));
                }
            }
        }
        if (fragments.size() > 0) {
            fragments.sort(FRAGMENT_COMPARATOR);
            // 从前向后找，如果后一个开始时间大于前一个的结束时间，则说明有重复的时间段，抛错
            List<Period> test = new ArrayList<>();
            List<Period> duplicated = new ArrayList<>();
            Period prev = fragments.get(0).getPeriod();
            test.add(prev);
            for (int i = 1; i < fragments.size(); i++) {
                Period current = fragments.get(i).getPeriod();
                if (current.getStart().before(prev.getEnd())) {
                    duplicated.add(Period.BUILDER.create(prev.getEnd(), current.getStart()));
                }
                test.add(current);
            }
            throwExceptionIfNotBlank(duplicated, "Duplicated period(s):");

            // 原始时间段减去全部切分段不为空，则表示有未匹配的时间段，补充上，并通过log进行警告提示
            List<Period> missing = Period.sub(Collections.singletonList(period), test, Period.BUILDER);
            if (missing.size() > 0) {
                for (Period p : missing) {
                    log.warn("from {} to {} is missing.",
                            Common.calendarToStr(p.getStart()),
                            Common.calendarToStr(p.getEnd()));
                    fragments.add(new BillingModel.Fragment<>(null, p));
                }
                fragments.sort(FRAGMENT_COMPARATOR);
            }
        } else {
            fragments.add(new BillingModel.Fragment<>(null, period));
            log.warn("slice result is empty.");
        }
        return fragments;
    }
}
