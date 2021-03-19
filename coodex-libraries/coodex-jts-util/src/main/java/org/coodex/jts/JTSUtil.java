/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

import org.coodex.util.Common;
import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.SelectableServiceLoader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.Objects;

public class JTSUtil {

    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final
    SelectableServiceLoader<Geometry, GeometryConvertService<Geometry>> CONVERT_SERVICE_LOADER
            = new LazySelectableServiceLoader<Geometry, GeometryConvertService<Geometry>>() {
    };

    /**
     * 地球赤道半径
     */
    private static final double EARTH_RADIUS = 6378137.0d;

    public static double[] lngLat2Mercator(double[] lngLat) {
        return lngLat2Mercator(lngLat[0], lngLat[1]);
    }

    public static double[] lngLat2Mercator(double lng, double lat) {
        double x = lng * Math.PI / 180 * EARTH_RADIUS;
        double a = lat * Math.PI / 180;
        double y = EARTH_RADIUS / 2 * Math.log((1.0d + Math.sin(a)) / (1.0d - Math.sin(a)));
        return new double[]{x, y};
    }

    public static double[] mercator2LngLat(double[] mercator) {
        return mercator2LngLat(mercator[0], mercator[1]);
    }

    public static double[] mercator2LngLat(double x, double y) {
        double lng = x / (EARTH_RADIUS * Math.PI / 180);
        double lat = y / (EARTH_RADIUS * Math.PI / 180);
        lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180)) - Math.PI / 2);
        return new double[]{lng, lat};
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static <T extends Geometry> T mercator2LngLat(T mercator) {
        return Common.cast(Objects.requireNonNull(CONVERT_SERVICE_LOADER.select(mercator)).toLngLat(mercator));
    }

    public static <T extends Geometry> T lngLat2Mercator(T lngLat) {
        return Common.cast(Objects.requireNonNull(CONVERT_SERVICE_LOADER.select(lngLat)).toMercator(lngLat));
    }

    public static Coordinate mercator2LngLat(Coordinate mercator) {
        double[] g = mercator2LngLat(mercator.getX(), mercator.getY());
        return new Coordinate(g[0], g[1]);
    }

    public static Coordinate lngLat2Mercator(Coordinate lngLat) {
        double[] g = lngLat2Mercator(lngLat.getX(), lngLat.getY());
        return new Coordinate(g[0], g[1]);
    }

    public static Coordinate[] mercator2LngLat(Coordinate[] mercator) {
        Coordinate[] coordinates = new Coordinate[mercator.length];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = mercator2LngLat(mercator[i]);
        }
        return coordinates;
    }

    public static Coordinate[] lngLat2Mercator(Coordinate[] lngLat) {
        Coordinate[] coordinates = new Coordinate[lngLat.length];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = lngLat2Mercator(lngLat[i]);
        }
        return coordinates;
    }
}
