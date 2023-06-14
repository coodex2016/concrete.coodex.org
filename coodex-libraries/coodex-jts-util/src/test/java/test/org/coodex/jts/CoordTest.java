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

package test.org.coodex.jts;

import org.coodex.jts.coord.Coord;
import org.coodex.jts.coord.CoordUtil;

import java.util.Arrays;

public class CoordTest {

    private static double[] trace(double[] pt, Coord from, Coord to){
        double[] newPt = CoordUtil.convert(pt, from, to);
        System.out.println(Arrays.toString(pt) + " " + from + " to "  + to + ": " + Arrays.toString(newPt));
        return newPt;
    }

    public static void main(String[] args) {
        double[] org = {110, 35};
        trace(trace(org, Coord.WGS84, Coord.GCJ02), Coord.GCJ02, Coord.WGS84);
        trace(trace(org, Coord.WGS84, Coord.BD09), Coord.BD09, Coord.WGS84);
        trace(trace(org, Coord.BD09, Coord.GCJ02), Coord.GCJ02, Coord.BD09);
        trace(trace(org, Coord.BD09, Coord.WGS84), Coord.WGS84, Coord.BD09);
        trace(trace(org, Coord.GCJ02, Coord.WGS84), Coord.WGS84, Coord.GCJ02);
        trace(trace(org, Coord.GCJ02, Coord.BD09), Coord.BD09, Coord.GCJ02);

    }
}
