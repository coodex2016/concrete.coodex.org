/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import com.alibaba.fastjson.JSON;
import org.coodex.util.Polygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-04-19.
 */
public class PolygonTest {

    public static class Geometry {
        public String type;
        public double[][][][] coordinates;
    }

    public static class Feature {
        public String type;
        public Map<String, String> properties;
        public Geometry geometry;
    }

    public static class FeatureCollection {
        public String type;
        public Feature[] features;
    }

    public static void main(String[] args) throws IOException {
        Map<String, Polygon> map = new HashMap<>();
        FeatureCollection featureCollection = JSON.parseObject(PolygonTest.class.getResourceAsStream("370100.json"), FeatureCollection.class);
        for(Feature feature: featureCollection.features){
            String name = feature.properties.get("name");
            List<Polygon.Point> points = new ArrayList<>();
            for(double[] point: feature.geometry.coordinates[0][0]){
                points.add(new Polygon.Point(point[0], point[1]));
            }
            Polygon polygon = new Polygon(points);
            map.put(name, polygon);
        }

        ////
        Polygon.Point point = new Polygon.Point(116.766434,36.588746);

        ///

        for(String name: map.keySet()){
            System.out.println(name + " : " + map.get(name).inPolygon(point));
        }
    }
}
