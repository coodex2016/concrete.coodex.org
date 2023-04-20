/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
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

package org.coodex.jts;

import org.locationtech.jts.geom.LinearRing;

import java.util.Objects;

public class AreaOfMercator implements AreaOf {
    @Override
    public double areaOf(LinearRing linearRing) {
        return JTSUtil.areaOf(JTSUtil.mercator2LngLat(linearRing), CoordType.LNG_LAT);
    }

    @Override
    public boolean accept(CoordType param) {
        return Objects.equals(param, CoordType.MERCATOR);
    }
}
