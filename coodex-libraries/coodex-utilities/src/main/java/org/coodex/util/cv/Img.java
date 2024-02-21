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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Function;

public class Img {

    public static boolean[][] map(String url, Function<Integer, Boolean> mapper) throws IOException {
        return map(url, mapper, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static boolean[][] map(String url, Function<Integer, Boolean> mapper, int l, int t, int r, int b) throws IOException {
//        l = Math.max(0, l);
//        t = Math.max(0, t);
//        r = r <= l ? Integer.MAX_VALUE : r;
//        b = b <= t ? Integer.MAX_VALUE : b;
//        BufferedImage image = ImageIO.read(new File(url));
//        r = Math.min(r, image.getWidth() - 1);
//        b = Math.min(b, image.getHeight() - 1);
//        int w = r - l + 1, h = b - t + 1;
//        System.out.printf("w: %d, h: %d", w, h);
//        boolean[][] bitmap = new boolean[h][w];
//        for (int i = l; i <= r; i++)
//            for (int j = t; j <= b; j++)
//                bitmap[j - t][i - l] = mapper.apply(image.getRGB(i, j));
//        return bitmap;
        try (InputStream inputStream = Files.newInputStream(new File(url).toPath())) {
            return map(inputStream, mapper, l, t, r, b);
        }
    }


    public static boolean[][] map(InputStream inputStream, Function<Integer, Boolean> mapper, int l, int t, int r, int b) throws IOException {
        l = Math.max(0, l);
        t = Math.max(0, t);
        r = r <= l ? Integer.MAX_VALUE : r;
        b = b <= t ? Integer.MAX_VALUE : b;
        BufferedImage image = ImageIO.read(inputStream);
        r = Math.min(r, image.getWidth() - 1);
        b = Math.min(b, image.getHeight() - 1);
        int w = r - l + 1, h = b - t + 1;
//        System.out.printf("w: %d, h: %d", w, h);
        boolean[][] bitmap = new boolean[h][w];
        for (int i = l; i <= r; i++)
            for (int j = t; j <= b; j++)
                bitmap[j - t][i - l] = mapper.apply(image.getRGB(i, j));
        return bitmap;
    }


    private static boolean[][] copyOf(boolean[][] bitmap) {
        int h = bitmap.length;
        int w = bitmap[0].length;
        boolean[][] v = new boolean[h][w];
        arrayCopy(bitmap, v);
        return v;
    }

    private static void arrayCopy(boolean[][] src, boolean[][] dist) {
        int h = src.length;
        int w = src[0].length;
        for (int i = 0; i < h; i++) {
            System.arraycopy(src[i], 0, dist[i], 0, w);
        }
    }

    private static void setTrue(boolean[][] array, int x, int y, int w, int h) {
        if (x < 0 || y < 0 || x >= w || y >= h)
            return;
        array[y][x] = true;
    }


    public static void grow(boolean[][] bitmap, int size, boolean cross) {
        boolean[][] kernel = new boolean[size * 2 + 1][size * 2 + 1];
        int len = size * 2 + 1;
        for (int i = 0; i <= size; i++) {
            for (int j = 0; j <= size; j++) {
                boolean cell = (!cross) || (i + j >= size);
                kernel[i][j] =
                        kernel[len - i - 1][len - j - 1] =
                                kernel[len - i - 1][j] =
                                        kernel[i][len - j - 1] =
                                                cell;

            }
        }
        grow(bitmap, kernel);
    }

    public static void grow(boolean[][] bitmap, boolean[][] kernel) {
        boolean[][] cache = copyOf(bitmap);
        int h = bitmap.length, w = bitmap[0].length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (bitmap[i][j]) {
//                    setTrue(cache, i, j, w, h);
                    int offsetX = kernel.length / 2;
                    for (int y = 0; y < kernel.length; y++) {
                        int offsetY = kernel[y].length / 2;
                        for (int x = 0; x < kernel[y].length; x++) {
                            if (kernel[x][y]) {
                                setTrue(cache, j - offsetX + x, i - offsetY + y, w, h);
                            }
                        }
                    }
                }
            }
        }
        arrayCopy(cache, bitmap);
    }

    private static int countOf(boolean[][] bitmap) {
        int count = 0;
        for (boolean[] array : bitmap)
            for (boolean b : array) {
                if (b) count++;
            }
//        System.out.println(count);
        return count;
    }

    public static float coverageOf(boolean[][] bitmap) {
        int h = bitmap.length, w = bitmap[0].length;
        return countOf(bitmap) * 1.f / (h * w);
    }
}
