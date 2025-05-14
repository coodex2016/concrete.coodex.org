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
import org.locationtech.jts.geom.Point;

import static org.coodex.jts.JTSUtil.*;


public class PointConverter implements GeometryConvertService<Point> {
    @Override
    public Point toMercator(Point lngLat) {
        return GEOMETRY_FACTORY.createPoint(lngLat2Mercator(lngLat.getCoordinate()));
    }

    @Override
    public Point toLngLat(Point mercator) {
        return GEOMETRY_FACTORY.createPoint(mercator2LngLat(mercator.getCoordinate()));
    }

    @Override
    public boolean accept(Point param) {
        return true;
    }
}
