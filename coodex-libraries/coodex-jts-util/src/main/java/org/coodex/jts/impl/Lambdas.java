/*
 * Copyright (c) 2016 - 2025 coodex.org (jujus.shen@126.com)
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

package org.coodex.jts.impl;

import org.coodex.functional.Function;
import org.coodex.jts.JTSUtil;
import org.locationtech.jts.geom.*;

public class Lambdas {

    public static final Function<Point, Point> lngLat2Mercator_Point = new Function<Point, Point>() {
        @Override
        public Point apply(Point point) {
            return JTSUtil.lngLat2Mercator(point);
        }
    };

    public static final Function<Polygon, Polygon> lngLat2Mercator_Polygon = new Function<Polygon, Polygon>() {
        @Override
        public Polygon apply(Polygon polygon) {
            return JTSUtil.lngLat2Mercator(polygon);
        }
    };

    public static final Function<LineString, LineString> lngLat2Mercator_LineString = new Function<LineString, LineString>() {
        @Override
        public LineString apply(LineString lineString) {
            return JTSUtil.lngLat2Mercator(lineString);
        }
    };

    public static final Function<LinearRing, LinearRing> lngLat2Mercator_LinearRing = new Function<LinearRing, LinearRing>() {
        @Override
        public LinearRing apply(LinearRing linearRing) {
            return JTSUtil.lngLat2Mercator(linearRing);
        }
    };

    public static final Function<Geometry, Geometry> lngLat2Mercator = new Function<Geometry, Geometry>() {
        @Override
        public Geometry apply(Geometry geometry) {
            return JTSUtil.lngLat2Mercator(geometry);
        }
    };

    public static final Function<Point, Point> mercator2LngLat_Point = new Function<Point, Point>() {
        @Override
        public Point apply(Point point) {
            return JTSUtil.mercator2LngLat(point);
        }
    };

    public static final Function<Polygon, Polygon> mercator2LngLat_Polygon = new Function<Polygon, Polygon>() {
        @Override
        public Polygon apply(Polygon polygon) {
            return JTSUtil.mercator2LngLat(polygon);
        }
    };

    public static final Function<LineString, LineString> mercator2LngLat_LineString = new Function<LineString, LineString>() {
        @Override
        public LineString apply(LineString lineString) {
            return JTSUtil.mercator2LngLat(lineString);
        }
    };

    public static final Function<LinearRing, LinearRing> mercator2LngLat_LinearRing = new Function<LinearRing, LinearRing>() {
        @Override
        public LinearRing apply(LinearRing linearRing) {
            return JTSUtil.mercator2LngLat(linearRing);
        }
    };

    public static final Function<Geometry, Geometry> mercator2LngLat = new Function<Geometry, Geometry>() {
        @Override
        public Geometry apply(Geometry geometry) {
            return JTSUtil.mercator2LngLat(geometry);
        }
    };

    public static final Function<Coordinate[], Coordinate[]> lnglat2Mercator_Coords = new Function<Coordinate[], Coordinate[]>() {
        @Override
        public Coordinate[] apply(Coordinate[] coordinates) {
            return JTSUtil.lngLat2Mercator(coordinates);
        }
    };

    public static final Function<Coordinate[], Coordinate[]> mercator2LanLat_Coords = new Function<Coordinate[], Coordinate[]>() {
        @Override
        public Coordinate[] apply(Coordinate[] coordinates) {
            return JTSUtil.mercator2LngLat(coordinates);
        }
    };
}
