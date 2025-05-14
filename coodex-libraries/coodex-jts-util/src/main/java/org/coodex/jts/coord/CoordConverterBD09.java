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

import java.util.Objects;

public class CoordConverterBD09 extends CoordConverterGCJ02 {
    private static final double x_pi = (Math.PI * 3000.0) / 180.0;

    @Override
    public double[] to84(double[] pt) {
        double[] gcj = bd09ToGCJ02(pt[0], pt[1]);
        return super.to84(gcj);
    }

    @Override
    public double[] from84(double[] pt84) {
        double[] gcj = super.from84(pt84);
        return gcj02ToBD09(gcj[0], gcj[1]);
    }


    private double[] gcj02ToBD09(double lng, double lat) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_pi);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_pi);
        double bd_lng = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new double[]{bd_lng, bd_lat};
    }

    public double[] bd09ToGCJ02(double bd_lon, double bd_lat) {
        double x = bd_lon - 0.0065;
        double y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lng = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new double[]{gg_lng, gg_lat};
    }


    @Override
    public boolean accept(Coord param) {
        return Objects.equals(param, Coord.BD09);
    }

}
