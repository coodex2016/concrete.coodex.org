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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class CoordTest {

    private static double[] trace(double[] pt, Coord from, Coord to) {
        double[] newPt = CoordUtil.convert(pt, from, to);
        System.out.println(Arrays.toString(pt) + " " + from + " to " + to + ": " + Arrays.toString(newPt));
        return newPt;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
//        double[] org = {110, 35};
//        trace(trace(org, Coord.WGS84, Coord.GCJ02), Coord.GCJ02, Coord.WGS84);
//        trace(trace(org, Coord.WGS84, Coord.BD09), Coord.BD09, Coord.WGS84);
//        trace(trace(org, Coord.BD09, Coord.GCJ02), Coord.GCJ02, Coord.BD09);
//        trace(trace(org, Coord.BD09, Coord.WGS84), Coord.WGS84, Coord.BD09);
//        trace(trace(org, Coord.GCJ02, Coord.WGS84), Coord.WGS84, Coord.GCJ02);
//        trace(trace(org, Coord.GCJ02, Coord.BD09), Coord.BD09, Coord.GCJ02);
//
//        int toF = 0xC12522D1;
//        System.out.printf("%08x%n", Float.floatToRawIntBits(-10.321f));
//        System.out.printf("%08x%n", Float.floatToIntBits(-10.321f));
//        String[] x = {"中", "国", "G", "B", "K", "编", "码"};
//        for (int i = 0; i < x.length; i++) {
//            System.out.printf("%s: char point %04x, gbk %s, utf-8 %s%n",
//                    x[i],
//                    x[i].charAt(0) & 0xFFFF,
//                    Common.base16Encode(x[i].getBytes("GBK")),
//                    Common.base16Encode(x[i].getBytes(StandardCharsets.UTF_8))
//            );
//        }

        str2BCDTest("");
        str2BCDTest("12345");
        str2BCDTest("1234567890");
        str2BCDTest("12345678901");
        str2BCDTest("202308171650");

    }

    static void str2BCDTest(String s) {
        System.out.printf("bcd of %s is: ", s);
        int l = s.length();
        for (int i = 0; i < 6; i++) {
            int a = (l - 12 + i * 2) < 0 ? 0 : (s.charAt(l - 12 + i * 2) - '0');
            int b = (l - 12 + i * 2 + 1) < 0 ? 0 : (s.charAt(l - 12 + i * 2 + 1) - '0');
            System.out.printf(" %02x", a << 4 | b);
        }
        System.out.println();
    }
}
