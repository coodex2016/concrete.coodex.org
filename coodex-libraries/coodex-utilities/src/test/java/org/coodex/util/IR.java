/*
 * Copyright (c) 2016 - 2024 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IR {
    public static double ZHE_SHE_LV_PC = 1.58;
    public static double ZHE_SHE_LV_KONGQI = 1;
    public static List<Point> routeState;
    private static boolean traceInfo = false;

    public static List<Point> jiaodian(Line line, Circle circle) {
        double a = line.a, b = line.b, c = circle.o.x, d = circle.o.y, r = circle.r;
        double A = 1 + a * a;
        double B = -2 * c + 2 * a * b - 2 * a * d;
        double C = c * c + b * b - 2 * b * d + d * d - r * r;
        double D = B * B - 4 * A * C;
        if (D < 0)
            return new ArrayList<>();
        double x1 = (-B + Math.sqrt(D)) / (2 * A);
        double x2 = (-B - Math.sqrt(D)) / (2 * A);
        List<Point> points = new ArrayList<>();
        points.add(new Point(x1, x1 * a + b));
        points.add(new Point(x2, x2 * a + b));
        return points;
    }

    public static double distance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public static Point jiaodian(Line line, Circle circle, Point ref, boolean nearly) {
        List<Point> points = jiaodian(line, circle);
        if (points.isEmpty())
            return null;
        double d1 = distance(points.get(0), ref), d2 = distance(points.get(1), ref);
        Point p1 = d1 <= d2 ? points.get(0) : points.get(1);
        Point p2 = d1 > d2 ? points.get(0) : points.get(1);
        return nearly ? p1 : p2;
    }

    public static Point ir_question(double x, double refX, Line ir, double r, double h) {
        routeState = new ArrayList<>();
        Circle c_inner = new Circle(0, 0, r);
        Circle c_outer = new Circle(0, 0, r + h);
        routeState.add(ir.point(refX));
        // 1 射线与外圆交点
        Point p = jiaodian(ir, c_outer, ir.point(refX), true);
        if (p == null) {
            if (traceInfo)
                System.out.println("外圆无交点");
            return null;
        }
        if (traceInfo)
            System.out.printf("\tfirst point: (%.6f, %.6f)\n", p.x, p.y);
        routeState.add(p);
        // 求入射角
        double theta_in = ir.jiajiao(new Line(p.y / p.x, 0));
        if (traceInfo)
            System.out.printf("\t入射角: %.6f°\n", Math.toDegrees(theta_in));

        // 折射角
        double theta_out = Math.asin(Math.sin(theta_in) * ZHE_SHE_LV_KONGQI / ZHE_SHE_LV_PC);
        if (traceInfo)
            System.out.printf("\t折射角: %.6f°\n", Math.toDegrees(theta_out));

        double alpha = Math.atan2(p.y, p.x); // 圆心角
        if (traceInfo)
            System.out.printf("\t圆心角: %.6f°\n", Math.toDegrees(alpha));
        double a = Math.tan(theta_out + alpha);
        if (traceInfo)
            System.out.printf("\t斜率角: %.6f°\n", Math.toDegrees(a));
        Line line_out = new Line(a, p.y - p.x * a);
        if (traceInfo)
            System.out.printf("\t第一段光路: y = %.6f * x + %.6f\n", line_out.a, line_out.b);

        // 内圆交点
        p = jiaodian(line_out, c_inner, p, true);
        if (p == null) {
            if (traceInfo)
                System.out.println("内圆无交点");
            return null;
        }
        if (traceInfo)
            System.out.printf("\t2nd point: (%.6f, %.6f)\n", p.x, p.y);
        routeState.add(p);

        theta_in = line_out.jiajiao(new Line(p.y / p.x, 0));
        if (traceInfo)
            System.out.printf("\t入射角2: %.6f°\n", Math.toDegrees(theta_in));

        theta_out = Math.asin(Math.sin(theta_in) * ZHE_SHE_LV_PC / ZHE_SHE_LV_KONGQI);
        if (traceInfo)
            System.out.printf("\t折射角2: %.6f°\n", Math.toDegrees(theta_out));

        alpha = Math.atan2(p.y, p.x); // 圆心角
        if (traceInfo)
            System.out.printf("\t圆心角: %.6f°\n", Math.toDegrees(alpha));
        a = Math.tan(theta_out + alpha);
        if (traceInfo)
            System.out.printf("\t斜率角: %.6f°\n", Math.toDegrees(a));
        line_out = new Line(a, p.y - p.x * a);
        if (traceInfo)
            System.out.printf("\t第二段光路: y = %.6f * x + %.6f\n", line_out.a, line_out.b);

        // 内圆向外
        p = jiaodian(line_out, c_inner, p, false);
        if (p == null) {
            if (traceInfo)
                System.out.println("内圆出射无交点");
            return null;
        }

        if (traceInfo)
            System.out.printf("\t3rd point: (%.6f, %.6f)\n", p.x, p.y);
        routeState.add(p);

        theta_in = line_out.jiajiao(new Line(p.y / p.x, 0));
        if (traceInfo)
            System.out.printf("\t入射角3: %.6f°\n", Math.toDegrees(theta_in));

        theta_out = Math.asin(Math.sin(theta_in) * ZHE_SHE_LV_KONGQI / ZHE_SHE_LV_PC);
        if (traceInfo)
            System.out.printf("\t折射角3: %.6f°\n", Math.toDegrees(theta_out));

        alpha = Math.atan2(p.y, p.x); // 圆心角
        if (traceInfo)
            System.out.printf("\t圆心角: %.6f°\n", Math.toDegrees(alpha));
        a = Math.tan(theta_out + alpha);
        if (traceInfo)
            System.out.printf("\t斜率角: %.6f°\n", Math.toDegrees(a));
        line_out = new Line(a, p.y - p.x * a);
        if (traceInfo)
            System.out.printf("\t第三段光路: y = %.6f * x + %.6f\n", line_out.a, line_out.b);

        // 射出点
        p = jiaodian(line_out, c_outer, p, true);
        if (p == null) {
            if (traceInfo)
                System.out.println("外圆出射无交点");
            return null;
        }
        if (traceInfo)
            System.out.printf("\t4th point: (%.6f, %.6f)\n", p.x, p.y);
        routeState.add(p);
        theta_in = line_out.jiajiao(new Line(p.y / p.x, 0));
        if (traceInfo)
            System.out.printf("\t入射角4: %.6f°\n", Math.toDegrees(theta_in));

        theta_out = Math.asin(Math.sin(theta_in) * ZHE_SHE_LV_PC / ZHE_SHE_LV_KONGQI);
        if (traceInfo)
            System.out.printf("\t折射角4: %.6f°\n", Math.toDegrees(theta_out));

        alpha = Math.atan2(p.y, p.x); // 圆心角
        if (traceInfo)
            System.out.printf("\t圆心角: %.6f°\n", Math.toDegrees(alpha));
        a = Math.tan(theta_out + alpha);
        if (traceInfo)
            System.out.printf("\t斜率角: %.6f°\n", Math.toDegrees(a));
        line_out = new Line(a, p.y - p.x * a);
        if (traceInfo)
            System.out.printf("\t第四段光路: y = %.6f * x + %.6f\n", line_out.a, line_out.b);
        routeState.add(line_out.point(x));
        return line_out.point(x);
    }
//

    /// / Example usage
//Arrays.asList(
//        new Line(0, 12), new Line(0, 4), new Line(0, -4), new Line(0, -12)
//).forEach(l -> {
//        Point pt = ir_question(26, -26, l, 30.f / 2, 1.5);
//        if (pt != null) {
//            System.out.printf("落点: (%.6f, %.6f)\n", pt.x, pt.y);
//        }
//    });
    public static void draw_image_and_save(double h, double d, List<List<Point>> routes, double interval) {
        BufferedImage bufferedImage = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 600, 600);
        g.setColor(Color.black);
        int r = (int) (d / 2 * 10);
        g.drawOval(300 - r, 300 - r, r * 2, r * 2);
        r += (int) (h * 10);
        g.drawOval(300 - r, 300 - r, r * 2, r * 2);

        for (List<Point> l : routes) {
            for (int i = 1; i < l.size(); i++) {
                Point s = l.get(i - 1);
                Point e = l.get(i);
                g.setColor(Color.red);
                g.drawLine((int) (s.x * 10 + 300), (int) (s.y * 10 + 300), (int) (e.x * 10 + 300),
                        (int) (e.y * 10 + 300));
            }
        }
//        routes.forEach(l -> {
//
//        });

        g.dispose();
        try {
            ImageIO.write(bufferedImage, "PNG", new File(String.format("ir_%.1fmm_%.1fmm_%.1fmm.PNG", interval, d, h)));
        } catch (IOException ignored) {

        }
    }

    public static void main(String[] args) {
        double h = 2.0;
        double x = 23.0;
        double i = 7;

        for (double d = 28.0; d < 51.0; d += 1) {
            List<List<Point>> routes = new ArrayList<>();
            final double D = d;

            for (Line l : Arrays.asList(new Line(0, i * 1.5), new Line(0, i * 0.5), new Line(0, -i * 0.5), new Line(0, -i * 1.5))) {
                Point pt = ir_question(D + h * 2 >= x * 2 ? 28 : x, -30, l, D / 2, h);
                if (pt != null) {
                    System.out.printf("内径: %.1f, 壁厚: %.1f, 射点：(%.1f, %.1f); 落点: (%.1f, %.1f)\n", D, h, -30.0,
                            l.point(x).y,
                            BigDecimal.valueOf(pt.x).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                            BigDecimal.valueOf(pt.y).setScale(1, RoundingMode.HALF_UP).doubleValue());
                    routes.add(new ArrayList<>(routeState));
                }
            }
//            Arrays.asList(new Line(0, i * 1.5), new Line(0, i * 0.5), new Line(0, -i * 0.5), new Line(0, -i * 1.5)).forEach(
//                    l -> {
//
//                    }
//            );
            System.out.println();
            // 出图
            draw_image_and_save(h, D, routes, i);
        }
    }

    public static class Point {
        private final double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Line {
        private final double a, b;

        /**
         * y = ax + b
         *
         * @param a
         * @param b
         */
        public Line(double a, double b) {
            this.a = a;
            this.b = b;
        }

        public Point point(double x) {
            return new Point(x, a * x + b);
        }

        public double jiajiao(Line another) {
            double a1 = a, a2 = another.a;
            return -Math.atan2(a2 - a1, 1 + a1 * a2);
        }
    }

    public static class Circle {
        private final Point o;
        private final double r;

        public Circle(Point o, double r) {
            this.o = o;
            this.r = r;
        }

        public Circle(double x, double y, double r) {
            this.o = new Point(x, y);
            this.r = r;
        }
    }
}
