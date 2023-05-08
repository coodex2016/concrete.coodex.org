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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.util.Arrays;

public class MetersGeometry {
    private final Coordinate base;
    private Geometry geometry;

    MetersGeometry(Geometry lngLat) {
        geometry = lngLat.copy();
        Coordinate[] coordinates = geometry.getCoordinates();
        double baseX = 180d, baseY = 180d;
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate c = coordinates[i];
            if (baseX > c.getX()) baseX = c.getX();
            if (baseY > c.getY()) baseY = c.getY();
        }
        base = new Coordinate(baseX, baseY);

        // 令，任意点(x1,y1)，投影坐标为(d((x1,y),(x,y)), d((x,y1),(x,y)))
        for (int i = 0, l = coordinates.length; i < l; i++) {
            Coordinate c = coordinates[i];
            Coordinate t = new Coordinate(JTSUtil.distanceLngLat(baseX, baseY, c.getX(), baseY), JTSUtil.distanceLngLat(baseX, baseY, baseX, c.getY()));
            c.setX(t.getX());
            c.setY(t.getY());
        }
    }

    MetersGeometry(Geometry meters, Coordinate base) {
        this.base = base;
        this.geometry = meters.copy();
    }

    public MetersGeometry xMove(double distance) {
        Coordinate[] coordinates = geometry.getCoordinates();
        for (int i = 0, l = coordinates.length; i < l; i++) {
            Coordinate c = coordinates[i];
            c.setX(c.getX() + distance);
        }
        return this;
    }

    public MetersGeometry yMove(double distance) {
        Coordinate[] coordinates = geometry.getCoordinates();
        for (int i = 0, l = coordinates.length; i < l; i++) {
            Coordinate c = coordinates[i];
            c.setY(c.getY() + distance);
        }
        return this;
    }

    public MetersGeometry move(double distance, double degree) {
        Coordinate[] coordinates = geometry.getCoordinates();
        for (int i = 0, l = coordinates.length; i < l; i++) {
            Coordinate c = coordinates[i];
            c.setX(c.getX() + Math.cos(degree / 180d * Math.PI) * distance);
            c.setY(c.getY() + Math.sin(degree / 180d * Math.PI) * distance);
        }
        return this;
    }

    public MetersGeometry simplify(double dt) {
        geometry = DouglasPeuckerSimplifier.simplify(geometry, dt);
        return this;
    }

    public MetersGeometry buffer(double distance) {
        geometry = geometry.buffer(distance);
        return this;
    }

    public MetersGeometry buffer(double distance, int quadrantSegments) {
        geometry = geometry.buffer(distance, quadrantSegments);
        return this;
    }

    public MetersGeometry buffer(double distance, int quadrantSegments, int endCapStyle) {
        geometry = geometry.buffer(distance, quadrantSegments, endCapStyle);
        return this;
    }

    public double getArea() {
        return geometry.getArea();
    }

    public Geometry toLngLat() {
        Geometry lngLat = geometry.copy();
        Arrays.stream(lngLat.getCoordinates()).forEach(coord -> {
            double x = coord.getX();
            double y = coord.getY();
            coord.setX(JTSUtil.xMove(base, x));
            coord.setY(JTSUtil.yMove(base, y));
        });
        return lngLat;
    }


    public MetersGeometry copy() {
        return new MetersGeometry(geometry, base);
    }
}
