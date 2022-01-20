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
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * 根据阈值裁剪内孔
     *
     * @param geometry  geometry
     * @param threshold 阈值，内孔占外边框面积的比例
     * @return 裁剪后的几何图形
     */
    public static Geometry crop(Geometry geometry, double threshold) {
        if (geometry instanceof MultiPolygon) {
            return cropMultiPolygon(geometry, threshold);
        } else if (geometry instanceof Polygon) {
            return cropPolygon(geometry, threshold);
        } else if (geometry instanceof GeometryCollection) {
            return crop(get2DGeometry(geometry), threshold);
        } else {
            return geometry;
        }
    }

    private static Geometry cropPolygon(Geometry geometry, double threshold) {
        if (geometry == null || geometry.isEmpty()) {
            return geometry;
        }
        Polygon polygon = (Polygon) geometry;
        if (polygon.getNumInteriorRing() == 0) {
            return geometry;
        }
        double outer = areaOf(polygon.getExteriorRing());
        List<LinearRing> holes = new ArrayList<>();
        for (int i = 0, l = polygon.getNumInteriorRing(); i < l; i++) {
            LinearRing hole = polygon.getInteriorRingN(i);
            if (areaOf(hole) / outer > threshold) {
                holes.add(hole);
            }
        }
        return Common.cast(holes.size() == 0 ? GEOMETRY_FACTORY.createPolygon(polygon.getExteriorRing()) :
                GEOMETRY_FACTORY.createPolygon(
                        polygon.getExteriorRing(),
                        holes.toArray(new LinearRing[0])
                ));
    }

    private static Geometry cropMultiPolygon(Geometry geometry, double threshold) {
        Geometry g = null;
        for (int i = 0, l = geometry.getNumGeometries(); i < l; i++) {
            if (g != null) {
                g = g.union(crop(geometry.getGeometryN(i), threshold));
            } else {
                g = geometry.getGeometryN(i);
            }
        }
        return g;
    }

    public static Geometry union2D(Geometry g1, Geometry g2) {
        return get2DGeometry(g1).union(get2DGeometry(g2));
    }

    public static Geometry intersection2D(Geometry g1, Geometry g2) {
        return get2DGeometry(get2DGeometry(g1).intersection(get2DGeometry(g2)));
    }

    public static Geometry difference2D(Geometry g1, Geometry g2) {
        return get2DGeometry(g1).difference(get2DGeometry(g2));
    }

    public static Geometry symDifference2D(Geometry g1, Geometry g2) {
        return get2DGeometry(g1).symDifference(get2DGeometry(g2));
    }


    public static double areaOf(Geometry lngLat) {
        if (lngLat instanceof MultiPolygon) {
            return areaOf((MultiPolygon) lngLat);
        } else if (lngLat instanceof Polygon) {
            return areaOf((Polygon) lngLat);
        } else if (lngLat instanceof LinearRing) {
            return areaOf((LinearRing) lngLat);
        } else if (lngLat instanceof GeometryCollection) {
            return areaOf(get2DGeometry(lngLat));
        } else {
            return 0d;
        }
    }

    public static Geometry get2DGeometry(Geometry geometry) {
        if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
            return geometry;
        } else if (geometry instanceof GeometryCollection) {
            Geometry polygon = null;
            for (int i = 0, l = geometry.getNumGeometries(); i < l; i++) {
                Geometry sub = get2DGeometry(geometry.getGeometryN(i));
                if (sub.getArea() > 0) {
                    polygon = polygon == null ? sub : polygon.union(sub);
                }
            }
            if (polygon != null) return polygon;
        }
        return JTSUtil.GEOMETRY_FACTORY.createEmpty(2);
    }

    private static double areaOf(MultiPolygon multiPolygon) {
        double area = 0d;
        for (int i = 0, size = multiPolygon.getNumGeometries(); i < size; i++) {
            area += areaOf(multiPolygon.getGeometryN(i));
        }
        return area;
    }

    private static double areaOf(Polygon polygon) {
        double area = areaOf(polygon.getExteriorRing());
        for (int i = 0, holes = polygon.getNumInteriorRing(); i < holes; i++) {
            area -= areaOf(polygon.getInteriorRingN(i));
        }
        return Math.max(0d, area);
    }

    private static boolean isLngLat(Geometry geometry) {
        Coordinate c = geometry.getCoordinate();
        return c == null || isLngLat(c);
    }

    private static boolean isLngLat(Coordinate coordinate) {
        return coordinate.getX() < 180d && coordinate.getX() > -180d &&
                coordinate.getY() < 90d && coordinate.getY() > -90;
    }

    private static double areaOf(LinearRing lngLatRing) {
        if (!isLngLat(lngLatRing)) {
            lngLatRing = mercator2LngLat(lngLatRing);
        }
        Coordinate[] coordinates = lngLatRing.getCoordinates();
        // 找到最小矩阵的左下角点作为原点
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
        return JTSUtil.GEOMETRY_FACTORY.createPolygon(newPoints).getArea();
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0d;
    }

    public static double distanceLngLat(double x1, double y1, double x2, double y2) {
        double radLat1 = rad(y1);
        double radLat2 = rad(y2);
        double a = radLat1 - radLat2;
        double b = rad(x1) - rad(x2);
        double s = 2 * Math.asin(
                Math.sqrt(
                        Math.pow(Math.sin(a / 2), 2)
                                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)
                )
        );
        return s * EARTH_RADIUS;
    }

}
