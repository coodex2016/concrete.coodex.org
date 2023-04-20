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

import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LinearRing;

import java.util.Objects;

import static org.coodex.jts.JTSUtil.distanceLngLat;

/**
 * 经纬度坐标系面积计算
 */
public class AreaOfLngLat implements AreaOf {
    @Override
    public double areaOf(LinearRing linearRing) {
        // 找到最小矩阵的左下角点作为原点，保证投影所有点都在第一象限
        Coordinate[] coordinates = linearRing.getCoordinates();
        double x = 180d, y = 180d;
        for (Coordinate c : coordinates) {
            x = Math.min(x, c.getX());
            y = Math.min(y, c.getY());
        }
        // 令，任意点(x1,y1)，投影坐标为(d((x1,y),(x,y)), d((x,y1),(x,y)))
        Coordinate[] newPoints = new Coordinate[coordinates.length];
        for (int i = 0, l = coordinates.length; i < l; i++) {
            Coordinate c = coordinates[i];
            newPoints[i] = new Coordinate(distanceLngLat(x, y, c.getX(), y), distanceLngLat(x, y, x, c.getY()));
        }
        return Area.ofRing(newPoints);
    }

    @Override
    public boolean accept(CoordType param) {
        return Objects.equals(param, CoordType.LNG_LAT);
    }

}
