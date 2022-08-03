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

package org.coodex.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class EndiannessTest {

    @Test
    public void test() {

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF},
                Endianness.BIG_ENDIAN.wordToByte(-1)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF},
                Endianness.BIG_ENDIAN.dwordToBytes(-1)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF},
                Endianness.BIG_ENDIAN.longToBytes(-1)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF},
                Endianness.LITTLE_ENDIAN.wordToByte(-1)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF},
                Endianness.LITTLE_ENDIAN.dwordToBytes(-1)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF},
                Endianness.LITTLE_ENDIAN.longToBytes(-1)
        );
    }

    @Test
    public void test2() {
        Assertions.assertArrayEquals(
                new byte[]{(byte) 0x80, (byte) 0x00},
                Endianness.BIG_ENDIAN.wordToByte(Short.MIN_VALUE)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00},
                Endianness.BIG_ENDIAN.dwordToBytes(Integer.MIN_VALUE)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0x80, 0, 0, 0, 0, 0, 0, 0},
                Endianness.BIG_ENDIAN.longToBytes(Long.MIN_VALUE)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0x00, (byte) 0x80},
                Endianness.LITTLE_ENDIAN.wordToByte(Short.MIN_VALUE)
        );

        Assertions.assertArrayEquals(
                new byte[]{0, 0, 0, (byte) 0x80},
                Endianness.LITTLE_ENDIAN.dwordToBytes(Integer.MIN_VALUE)
        );

        Assertions.assertArrayEquals(
                new byte[]{0, 0, 0, 0, 0, 0, 0, (byte) 0x80},
                Endianness.LITTLE_ENDIAN.longToBytes(Long.MIN_VALUE)
        );
    }

    @Test
    public void test3() {

        Assertions.assertArrayEquals(
                new byte[]{0x40, 0x49, 0x0F, (byte) 0xDB},
                Endianness.BIG_ENDIAN.floatToBytes((float) Math.PI)
        );

        Assertions.assertArrayEquals(
                new byte[]{0x40, 0x09, 0x21, (byte) 0xFB, 0x54, 0x44, 0x2D, 0x18},
                Endianness.BIG_ENDIAN.doubleToBytes(Math.PI)
        );

        Assertions.assertArrayEquals(
                Endianness.reverseBytes(new byte[]{0x40, 0x49, 0x0F, (byte) 0xDB}),
                Endianness.LITTLE_ENDIAN.floatToBytes((float) Math.PI)
        );

        Assertions.assertArrayEquals(
                Endianness.reverseBytes(new byte[]{0x40, 0x09, 0x21, (byte) 0xFB, 0x54, 0x44, 0x2D, 0x18}),
                Endianness.LITTLE_ENDIAN.doubleToBytes(Math.PI)
        );
    }

    @Test
    public void test4() {
        long longValue = 0x11223344889900AAL;
        Assertions.assertArrayEquals(
                new byte[]{0x11, 0x22, 0x33, 0x44, (byte) 0x88, (byte) 0x99, 0x00, (byte) 0xAA},
                Endianness.BIG_ENDIAN.longToBytes(longValue)
        );

        Assertions.assertArrayEquals(
                new byte[]{(byte) 0x88, (byte) 0x99, 0x00, (byte) 0xAA},
                Endianness.BIG_ENDIAN.dwordToBytes((int) longValue)
        );

        Assertions.assertArrayEquals(
                new byte[]{0x00, (byte) 0xAA},
                Endianness.BIG_ENDIAN.wordToByte((short) longValue & 0xFFFF)
        );

        Assertions.assertArrayEquals(
                Endianness.reverseBytes(new byte[]{0x11, 0x22, 0x33, 0x44, (byte) 0x88, (byte) 0x99, 0x00, (byte) 0xAA}),
                Endianness.LITTLE_ENDIAN.longToBytes(longValue)
        );

        Assertions.assertArrayEquals(
                Endianness.reverseBytes(new byte[]{(byte) 0x88, (byte) 0x99, 0x00, (byte) 0xAA}),
                Endianness.LITTLE_ENDIAN.dwordToBytes((int) longValue)
        );

        Assertions.assertArrayEquals(
                Endianness.reverseBytes(new byte[]{0x00, (byte) 0xAA}),
                Endianness.LITTLE_ENDIAN.wordToByte((short) longValue & 0xFFFF)
        );
    }

    @Test
    public void test5() {
        byte[] buf = new byte[]{0x11, 0x22, 0x33, 0x44, (byte) 0x88, (byte) 0x99, 0x00, (byte) 0xAA};
        long longValue = 0x11223344889900AAL;
        Assertions.assertEquals(
                longValue, Endianness.BIG_ENDIAN.readLong(buf, 0)
        );

        Assertions.assertEquals(
                0x11223344, Endianness.BIG_ENDIAN.readDword(buf, 0)
        );

        Assertions.assertEquals(
                (int) longValue, Endianness.BIG_ENDIAN.readDword(buf, 4)
        );

        Assertions.assertEquals(
                (int) (longValue & 0xFFFF), Endianness.BIG_ENDIAN.readWord(buf, 6)
        );

        buf = Endianness.reverseBytes(buf);

        Assertions.assertEquals(
                longValue, Endianness.LITTLE_ENDIAN.readLong(buf, 0)
        );

        Assertions.assertEquals(
                0x11223344, Endianness.LITTLE_ENDIAN.readDword(buf, 4)
        );

        Assertions.assertEquals(
                (int) longValue, Endianness.LITTLE_ENDIAN.readDword(buf, 0)
        );

        Assertions.assertEquals(
                (int) (longValue & 0xFFFF), Endianness.LITTLE_ENDIAN.readWord(buf, 0)
        );

    }

    @Test
    public void test6() {
        Assertions.assertEquals(
                Endianness.BIG_ENDIAN.readFloat(new byte[]{0x40, 0x49, 0x0F, (byte) 0xDB}, 0),
                (float) Math.PI
        );

        Assertions.assertEquals(
                Endianness.BIG_ENDIAN.readDouble(new byte[]{0x40, 0x09, 0x21, (byte) 0xFB, 0x54, 0x44, 0x2D, 0x18}, 0),
                Math.PI
        );

        Assertions.assertEquals(
                Endianness.LITTLE_ENDIAN.readFloat(Endianness.reverseBytes(new byte[]{0x40, 0x49, 0x0F, (byte) 0xDB}), 0),
                (float) Math.PI
        );

        Assertions.assertEquals(
                Endianness.LITTLE_ENDIAN.readDouble(Endianness.reverseBytes(new byte[]{0x40, 0x09, 0x21, (byte) 0xFB, 0x54, 0x44, 0x2D, 0x18}), 0),
                Math.PI
        );

    }
}
