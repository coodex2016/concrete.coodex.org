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

package test.org.coodex.jts;

import org.coodex.jts.JTSUtil;
import org.locationtech.jts.geom.*;

import java.util.Arrays;

public class ConvertServiceTest {

    private static Coordinate[] coordinates = new Coordinate[]{
            new Coordinate(110d, 40d),
            new Coordinate(110d, 50d),
            new Coordinate(120d, 50d),
            new Coordinate(120d, 40d),
            new Coordinate(110d, 40d)

    };

    private static <T extends Geometry> T print(T geometry) {
        System.out.println(geometry.toText());
        return geometry;
    }

    private static <T extends Geometry> T test(T geometry) {
        return print(JTSUtil.mercator2LngLat(print(JTSUtil.lngLat2Mercator(geometry))));
    }

    public static void main(String[] args) {
        Point point = test(JTSUtil.GEOMETRY_FACTORY.createPoint(coordinates[0]));

        LineString lineString = test(JTSUtil.GEOMETRY_FACTORY.createLineString(coordinates));

        LinearRing linearRing = test(JTSUtil.GEOMETRY_FACTORY.createLinearRing(coordinates));

        MultiPoint multiPoint = test(new MultiPoint(Arrays.stream(coordinates)
                .map(c -> JTSUtil.GEOMETRY_FACTORY.createPoint(c))
                .toArray(value -> new Point[coordinates.length])
                , JTSUtil.GEOMETRY_FACTORY));

        test(new GeometryCollection(new Geometry[]{
                point, linearRing, lineString, multiPoint
        }, JTSUtil.GEOMETRY_FACTORY));

    }
}
