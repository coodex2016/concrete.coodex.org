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

package org.coodex.jts.impl;

import org.coodex.jts.GeometryConvertService;
import org.coodex.jts.JTSUtil;
import org.coodex.util.Common;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.coodex.jts.JTSUtil.GEOMETRY_FACTORY;

public class GeometryCollectionConverter implements GeometryConvertService<GeometryCollection> {


    private static final GCCFunction<MultiPoint, Point> MULTI_POINT_POINT_GCC_FUNCTION = geometries -> new MultiPoint(geometries.toArray(new Point[0]), GEOMETRY_FACTORY);

    private static final GCCFunction<MultiLineString, LineString> MULTI_LINE_STRING_LINE_STRING_GCC_FUNCTION = geometries -> new MultiLineString(geometries.toArray(new LineString[0]), GEOMETRY_FACTORY);

    private static final GCCFunction<MultiPolygon, Polygon> MULTI_POLYGON_POLYGON_GCC_FUNCTION = geometries -> new MultiPolygon(geometries.toArray(new Polygon[0]), GEOMETRY_FACTORY);

    private static final GCCFunction<GeometryCollection, Geometry> DEFAULT = geometries -> new GeometryCollection(geometries.toArray(new Geometry[0]), GEOMETRY_FACTORY);


    @Override
    public GeometryCollection toMercator(GeometryCollection source) {
        //noinspection DuplicatedCode
        if (source instanceof MultiPoint) {
            return MULTI_POINT_POINT_GCC_FUNCTION.apply((MultiPoint) source, JTSUtil::lngLat2Mercator);
        } else if (source instanceof MultiLineString) {
            return MULTI_LINE_STRING_LINE_STRING_GCC_FUNCTION.apply((MultiLineString) source, JTSUtil::lngLat2Mercator);
        } else if (source instanceof MultiPolygon) {
            return MULTI_POLYGON_POLYGON_GCC_FUNCTION.apply((MultiPolygon) source, JTSUtil::lngLat2Mercator);
        } else {
            return DEFAULT.apply(source, JTSUtil::lngLat2Mercator);
        }
    }

    @Override
    public GeometryCollection toLngLat(GeometryCollection mercator) {
        //noinspection DuplicatedCode
        if (mercator instanceof MultiPoint) {
            return MULTI_POINT_POINT_GCC_FUNCTION.apply((MultiPoint) mercator, JTSUtil::mercator2LngLat);
        } else if (mercator instanceof MultiLineString) {
            return MULTI_LINE_STRING_LINE_STRING_GCC_FUNCTION.apply((MultiLineString) mercator, JTSUtil::mercator2LngLat);
        } else if (mercator instanceof MultiPolygon) {
            return MULTI_POLYGON_POLYGON_GCC_FUNCTION.apply((MultiPolygon) mercator, JTSUtil::mercator2LngLat);
        } else {
            return DEFAULT.apply(mercator, JTSUtil::mercator2LngLat);
        }
    }

    @Override
    public boolean accept(GeometryCollection param) {
        return true;
    }

    interface GCCFunction<M extends GeometryCollection, S extends Geometry> extends BiFunction<M, Function<S, S>, M> {
         M create(List<S> geometries);

        @Override
        default M apply(M source, Function<S, S> func) {
            List<S> geometries = new ArrayList<>();
            for (int i = 0; i < source.getNumGeometries(); i++) {
                geometries.add(func.apply(Common.cast(source.getGeometryN(i))));
            }
            return create(geometries);
        }
    }
}
