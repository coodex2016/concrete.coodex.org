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

package org.coodex.jts.coord;

import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.SelectableServiceLoader;

import java.util.Objects;

public class CoordUtil {
    static final double EE = 0.00669342162296594323;
    static final double A = 6378245.0d;
    private static final SelectableServiceLoader<Coord, CoordConverter> CONVERTORS = new LazySelectableServiceLoader<Coord, CoordConverter>() {
    };

    static double transformlat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret = getRet(lng, lat, ret);
        ret += ((160.0 * Math.sin((lat / 12.0) * Math.PI) + 320 * Math.sin((lat * Math.PI) / 30.0)) * 2.0) / 3.0;
        return ret;
    }

    static double getRet(double lng, double lat, double ret) {
        ret += ((20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0) / 3.0;
        ret += ((20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin((lat / 3.0) * Math.PI)) * 2.0) / 3.0;
        return ret;
    }

    static double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret = getRet(lng, lng, ret);
        ret += ((150.0 * Math.sin((lng / 12.0) * Math.PI) + 300.0 * Math.sin((lng / 30.0) * Math.PI)) * 2.0) / 3.0;
        return ret;
    }

    /**
     * @param pt   坐标点，double[2]
     * @param from pt坐标系
     * @param to   要转换到的坐标系
     * @return 目标坐标系的经纬度数组, double[2]
     */
    public static double[] convert(double[] pt, Coord from, Coord to) {
        if (Objects.equals(from, to)) return pt;
        CoordConverter cFrom = CONVERTORS.select(from);
        CoordConverter cTo = CONVERTORS.select(to);
        return cTo.from84(cFrom.to84(pt));
    }

}
