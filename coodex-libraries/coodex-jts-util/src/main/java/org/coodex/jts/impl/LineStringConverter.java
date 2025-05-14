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

import org.coodex.functional.BiFunction;
import org.coodex.functional.Function;
import org.coodex.jts.GeometryConvertService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;

import static org.coodex.jts.JTSUtil.GEOMETRY_FACTORY;

public class LineStringConverter implements GeometryConvertService<LineString> {
    static final BiFunction<LineString, Function<Coordinate[], Coordinate[]>, LineString> LINESTRING_CONVERTER_FUNCTION
            = new BiFunction<LineString, Function<Coordinate[], Coordinate[]>, LineString>() {

        @Override
        public LineString apply(LineString source, Function<Coordinate[], Coordinate[]> func) {
            return source instanceof LinearRing ?
                    GEOMETRY_FACTORY.createLinearRing(func.apply(source.getCoordinates())) :
                    GEOMETRY_FACTORY.createLineString(func.apply(source.getCoordinates()));
        }
    };


    @Override
    public LineString toMercator(LineString lngLat) {
        return LINESTRING_CONVERTER_FUNCTION.apply(lngLat, Lambdas.lnglat2Mercator_Coords);
    }

    @Override
    public LineString toLngLat(LineString mercator) {
        return LINESTRING_CONVERTER_FUNCTION.apply(mercator, Lambdas.mercator2LanLat_Coords);
    }

    @Override
    public boolean accept(LineString param) {
        return true;
    }
}
