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

import static org.coodex.jts.coord.CoordUtil.*;

@SuppressWarnings({"DuplicatedCode", "SpellCheckingInspection"})
public class CoordConverterGCJ02 implements CoordConverter{
    @Override
    public double[] to84(double[] pt) {
        double lng = pt[0], lat = pt[1];
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = (lat / 180.0) * Math.PI;
        double magic = Math.sin(radlat);
        magic = 1 - EE * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / (((A * (1 - EE)) / (magic * sqrtmagic)) * Math.PI);
        dlng = (dlng * 180.0) / ((A / sqrtmagic) * Math.cos(radlat) * Math.PI);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[]{lng * 2 - mglng, lat * 2 - mglat};
    }

    @Override
    public double[] from84(double[] pt84) {
        double lng = pt84[0], lat = pt84[1];
        double dlat = transformlat(lng - 105.0, lat - 35.0);
        double dlng = transformlng(lng - 105.0, lat - 35.0);
        double radlat = (lat / 180.0) * Math.PI;
        double magic = Math.sin(radlat);
        magic = 1 - EE * magic * magic;
        double sqrtmagic = Math.sqrt(magic);
        dlat = (dlat * 180.0) / (((A * (1 - EE)) / (magic * sqrtmagic)) *Math.PI);
        dlng = (dlng * 180.0) / ((A / sqrtmagic) * Math.cos(radlat) * Math.PI);
        double mglat = lat + dlat;
        double mglng = lng + dlng;
        return new double[]{mglng, mglat};
    }

    @Override
    public boolean accept(Coord param) {
        return Objects.equals(param, Coord.GCJ02);
    }
}
