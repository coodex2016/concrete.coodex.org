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

package org.coodex.util.cv;

public class Color {

    private Color() {
    }

    public static int rgb(int r, int g, int b) {
        return ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xFF);
    }

    /**
     * @param rgb 0x00RRGGBB
     * @return
     */
    public static HSVL rgb2hsvl(int rgb) {
        return new HSVL(rgb);
    }

    public static HSVL rgb2hsvl(int r, int g, int b) {
        return rgb2hsvl(r / 255.f, g / 255.f, b / 255.f);
    }

    public static HSVL rgb2hsvl(float r, float g, float b) {
        return new HSVL(r, g, b);
    }

    public static HSVL hsv(float h, float s, float v) {
        return new HSVL(h, s, v, 0);
    }


    public static HSVL hsl(float h, float s, float l) {
        return new HSVL(h, s, 0, l);
    }

    public static void main(String[] args) {
//        System.out.println(rgb2hsvl(150, 75, 13));
//        System.out.println(rgb2hsvl(255, 153, 22));

    }

    public static class HSVL {
        public final float h, s, v, l;

        private HSVL(float h, float s, float v, float l) {
            this.h = h;
            this.s = s;
            this.v = v;
            this.l = l;
        }

        HSVL(float r, float g, float b) {
            float max = Math.max(Math.max(r, g), b);
            float min = Math.min(Math.min(r, g), b);
            float delta = max - min;
            l = (max + min) / 2;
            if (delta == 0) {
                h = 0;
            } else if (max == r) {
                h = (g - b) / delta * 60.f + (g >= b ? 0f : 360f);
            } else if (max == g) {
                h = (b - r) / delta * 60.f + 120;
            } else {
                h = (r - g) / delta * 60.f + 240;
            }
            if (l == 0 || delta == 0) {
                s = 0;
            } else if (l <= 0.5) {
                s = delta / (2 * l);
            } else {
                s = delta / (2 - 2 * l);
            }
            v = max;
        }


        HSVL(int rgb) {
            this(((rgb >> 16) & 0xff) / 255.0f, ((rgb >> 8) & 0xff) / 255.0f, (rgb & 0xff) / 255.0f);
        }

        @Override
        public String toString() {
            return "HSVL{" +
                    "h=" + h +
                    ", s=" + s +
                    ", v=" + v +
                    ", l=" + l +
                    '}';
        }
    }
//
//    public class RGB {
//        private final int r, g, b;
//
//        public RGB(int rgb) {
//            this((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff);
//        }
//
//        public RGB(int r, int g, int b) {
//            this.r = r;
//            this.g = g;
//            this.b = b;
//        }
//
//        public int getR() {
//            return r;
//        }
//
//        public int getG() {
//            return g;
//        }
//
//        public int getB() {
//            return b;
//        }
//    }
}
