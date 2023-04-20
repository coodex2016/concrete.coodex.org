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

import org.coodex.util.SelectableService;
import org.locationtech.jts.geom.*;

import static org.coodex.jts.JTSUtil.get2DGeometry;

public interface AreaOf extends SelectableService<AreaOf.CoordType> {
    enum CoordType {
        LNG_LAT,// 经纬度坐标系
        METERS, // 米单位坐标系
        MERCATOR, // 墨卡托坐标系
        @Deprecated
        COMPATIBLE //兼容模式，不推荐使用
    }

    default double areaOf(Geometry geometry){
        if (geometry instanceof MultiPolygon) {
            return areaOf((MultiPolygon) geometry);
        } else if (geometry instanceof Polygon) {
            return areaOf((Polygon) geometry);
        } else if (geometry instanceof LinearRing) {
            return areaOf((LinearRing) geometry);
        } else if (geometry instanceof GeometryCollection) {
            return areaOf(get2DGeometry(geometry));
        } else {
            return 0d;
        }
    }

    default double areaOf(MultiPolygon multiPolygon){
        double area = 0d;
        for (int i = 0, size = multiPolygon.getNumGeometries(); i < size; i++) {
            area += areaOf(multiPolygon.getGeometryN(i));
        }
        return area;
    }

    default double areaOf(Polygon polygon){
        double area = areaOf(polygon.getExteriorRing());
        for (int i = 0, holes = polygon.getNumInteriorRing(); i < holes; i++) {
            area -= areaOf(polygon.getInteriorRingN(i));
        }
        return Math.max(0d, area);
    }

    double areaOf(LinearRing linearRing);
}
