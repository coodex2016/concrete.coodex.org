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
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.coodex.jts.JTSUtil.GEOMETRY_FACTORY;


public class PolygonConverter implements GeometryConvertService<Polygon> {
    static BiFunction<Polygon, Function<LinearRing, LinearRing>, Polygon> POLYGON_CONVERTER =
            (source, func) -> {
                LinearRing shell = func.apply(source.getExteriorRing());
                List<LinearRing> holes = new ArrayList<>();
                for (int i = 0; i < source.getNumInteriorRing(); i++) {
                    holes.add(func.apply(source.getInteriorRingN(i)));
                }
                return holes.size() > 0 ?
                        GEOMETRY_FACTORY.createPolygon(shell, holes.toArray(new LinearRing[0])) :
                        GEOMETRY_FACTORY.createPolygon(shell);
            };

    @Override
    public Polygon toMercator(Polygon lngLat) {
        return POLYGON_CONVERTER.apply(lngLat, JTSUtil::lngLat2Mercator);
    }

    @Override
    public Polygon toLngLat(Polygon mercator) {
        return POLYGON_CONVERTER.apply(mercator, JTSUtil::mercator2LngLat);
    }

    @Override
    public boolean accept(Polygon param) {
        return true;
    }
}
