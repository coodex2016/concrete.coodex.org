/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

import java.util.List;

/**
 * Created by davidoff shen on 2017-04-19.
 */
public class Polygon {

    private final static InAlgorithm inAlgorithm = new TurnoverNumberAlgorithm();
    private final Point[] points;


    public Polygon(List<Point> points) {
        this.points = points.toArray(new Point[0]);
    }

    public boolean inPolygon(Point point) {
        return inAlgorithm.in(point, points);
    }

    interface InAlgorithm {
        boolean in(Point point, Point[] polygon);
    }

    public static class Point {
        private final double x;
        private final double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    /**
     * 射线法，修改自http://www.html-js.com/article/1528
     */
    static class RayAlgorithm implements InAlgorithm {

        @Override
        public boolean in(Point p, Point[] poly) {
            double px = p.x, py = p.y;
            boolean flag = false;

            for (int i = 0, l = poly.length, j = l - 1; i < l; j = i, i++) {
                double sx = poly[i].x, sy = poly[i].y, tx = poly[j].x, ty = poly[j].y;

                // 点与多边形顶点重合
                if ((sx == px && sy == py) || (tx == px && ty == py)) {
                    return true;
                }

                // 判断线段两端点是否在射线两侧
                if ((sy < py && ty >= py) || (sy >= py && ty < py)) {
                    // 线段上与射线 Y 坐标相同的点的 X 坐标
                    double x = sx + (py - sy) * (tx - sx) / (ty - sy);

                    // 点在多边形的边上
                    if (x == px) {
                        return true;
                    }

                    // 射线穿过多边形的边界
                    if (x > px) {
                        flag = !flag;
                    }
                }
            }

            // 射线穿过多边形边界的次数为奇数时点在多边形内
            return flag;
        }
    }

    /**
     * 回转数法，修改自http://www.html-js.com/article/1538
     */
    static class TurnoverNumberAlgorithm implements InAlgorithm {

        @Override
        public boolean in(Point p, Point[] poly) {
            double px = p.x, py = p.y, sum = 0;

            for (int i = 0, l = poly.length, j = l - 1; i < l; j = i, i++) {
                double sx = poly[i].x, sy = poly[i].y, tx = poly[j].x, ty = poly[j].y;

                // 点与多边形顶点重合或在多边形的边上
                if ((sx - px) * (px - tx) >= 0 &&
                        (sy - py) * (py - ty) >= 0 &&
                        (px - sx) * (ty - sy) == (py - sy) * (tx - sx)) {
                    return true;
                }

                // 点与相邻顶点连线的夹角
                double angle = Math.atan2(sy - py, sx - px) - Math.atan2(ty - py, tx - px);

                // 确保夹角不超出取值范围（-π 到 π）
                if (angle >= Math.PI) {
                    angle = angle - Math.PI * 2;
                } else if (angle <= -Math.PI) {
                    angle = angle + Math.PI * 2;
                }

                sum += angle;
            }

            // 计算回转数并判断点和多边形的几何关系
            return Math.round(sum / Math.PI) != 0;
        }
    }


}
