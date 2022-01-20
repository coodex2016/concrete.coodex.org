/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

import com.alibaba.fastjson.JSON;
import org.coodex.util.Common;
import org.locationtech.jts.geom.Coordinate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CoordinatesLoader {

    public static final String COORDINATES_PATH = "coordinateCase.json";

    public static Coordinate[] randomCoordinates(int dots) {
        Coordinate[] coordinates = new Coordinate[dots];
//        for (int i = 0; i < dots; i++) {
//            coordinates[i] = new Coordinate(
//                    Common.random(100000),
//                    Common.random(100000)
//            );
//        }
        coordinates[0] = new Coordinate(
                Common.random(100000),
                Common.random(100000)
        );
        for (int i = 1; i < dots; i++) {
            coordinates[i] = randomCoordinate(coordinates[i - 1]);
        }
        return coordinates;
    }

    private static Coordinate randomCoordinate(Coordinate coordinate) {
        int angle = Common.random(360);
        int distance = Common.random(6) + 3;
        double x = coordinate.getX() + Math.cos(angle) * distance;
        double y = coordinate.getY() + Math.sin(angle) * distance;
        return new Coordinate(x, y);
    }


    public static void main(String[] args) throws IOException {
        File f = new File(COORDINATES_PATH);
        if (!f.exists()) f.createNewFile();
        try (OutputStream outputStream = new FileOutputStream(f)) {
            outputStream.write(JSON.toJSONString(Arrays.stream(
                    randomCoordinates(40000)).map(P::new).collect(Collectors.toList())).getBytes(StandardCharsets.UTF_8));
        }
        System.out.println(f.getAbsolutePath() + " created");

        System.out.print(loader().length);
    }

    public static Coordinate[] loader() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(COORDINATES_PATH))
        )) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return Arrays.stream(JSON.parseObject(builder.toString(), P[].class)).map(P::toCoordinate).toArray(Coordinate[]::new);
        }
    }

    static class P {

        public double x;
        public double y;

        public P() {
        }

        public P(Coordinate c) {
            this(c.getX(), c.getY());
        }

        public P(double x, double y) {
            this.x = x;
            this.y = y;
        }

        Coordinate toCoordinate() {
            return new Coordinate(x, y);
        }
    }
}
