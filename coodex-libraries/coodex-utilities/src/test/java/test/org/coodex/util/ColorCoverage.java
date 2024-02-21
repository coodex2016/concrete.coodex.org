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

package test.org.coodex.util;

import org.coodex.util.Common;
import org.coodex.util.cv.Color;
import org.coodex.util.cv.Img;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public class ColorCoverage {

    private static void saveTo(BufferedImage bufferedImage, String fileName) throws IOException {
        ImageIO.write(bufferedImage, "png", new File(fileName + ".png"));
    }


    private static void toPic(boolean[][] bitmap, String fileName, Float coverage) throws IOException {
        int w = bitmap[0].length, h = bitmap.length;
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++) {
                bufferedImage.setRGB(i, j, bitmap[j][i] ? 0xFFFFFF : 0);
            }
        Graphics2D graphics = bufferedImage.createGraphics();
        int fontSize = bitmap[0].length / 30;
        Font font = new Font("", Font.PLAIN, fontSize);
        graphics.setFont(font);
        graphics.setColor(java.awt.Color.MAGENTA);
        graphics.drawString(String.format("Coverage: %.2f %%", coverage * 100f), 10, 10 + fontSize);
        saveTo(bufferedImage, fileName);
    }

    private static void testCase(String img) throws IOException {
        try (InputStream inputStream = Common.getResource("/img/" + img).openStream()) {
            boolean[][] bitmap = Img.map(inputStream,
                    hsvlFilter(Color.hsv(24f, .1f, .65f),
                            Color.hsv(47f, .58f, .99f)),
                    0, 0, -1, -1);
            toPic(bitmap, img + "_before", Img.coverageOf(bitmap));
            Img.grow(bitmap, 4, true);
//            System.out.println("coverageOf( " + img + " ):" + Img.coverageOf(bitmap));
            toPic(bitmap, img + "_after", Img.coverageOf(bitmap));
        }
    }

    public static void main(String[] args) throws IOException {

        String[] files = {"01.jpg", "02.jpg", "03.jpg", "04.jpg", "05.jpg", "06.jpg", "07.jpg", "08.jpg", "09.jpg", "10.jpg", "11.jpg",
                "12.png", "13.jpg", "14.jpeg", "15.jpg", "16.jpg"};
        for (String file : files) {
            testCase(file);
        }
//
//        boolean[][] bitmap = Img.map(
//                "/Users/shenhainan/Documents/jiegan07.webp",
//                hsvlFilter(Color.hsv(25.75f, 75f / 255, 150f / 255), Color.hsv(44.25f, 153f / 255, 1f)),
//                437, 116, 736, 358);
//
//
//        toPic(bitmap, "before");
//        Img.grow(bitmap, 2, true
////                new boolean[][]{
////                        {false, false, true, false, false},
////                        {false, true, true, true, false},
////                        {true, true, true, true, true},
////                        {false, true, true, true, false},
////                        {false, false, true, false, false},
////                }
////                new boolean[][]{
////                        {false, true, false},
////                        {true, true, true},
////                        {false, true, false},
////                }
//        );
//        System.out.println(Img.coverageOf(bitmap));
//        toPic(bitmap, "after");
    }

    private static Function<Integer, Boolean> hsvlFilter(Color.HSVL hsv1, Color.HSVL hsv2) {
//        Color.HSVL hsvl1 = Color.rgb2hsvl(rgb1);
//        Color.HSVL hsvl2 = Color.rgb2hsvl(rgb2);
        float hMin = 25.75f;//Math.min(hsvl1.h, hsvl2.h);
        float hMax = 44.25f;//Math.max(hsvl1.h, hsvl2.h);
        float sMin = Math.min(hsv1.s, hsv2.h);
        float sMax = Math.max(hsv1.s, hsv2.h);
        float vMin = Math.min(hsv1.v, hsv2.v);
        float vMax = Math.max(hsv1.v, hsv2.v);


        return rgb -> {
            Color.HSVL hsvl = Color.rgb2hsvl(rgb);
            return hsvl.h <= hMax && hsvl.h >= hMin
                    && hsvl.s <= sMax && hsvl.s > sMin
                    && hsvl.v >= vMin && hsvl.v <= vMax;
        };
    }

}
