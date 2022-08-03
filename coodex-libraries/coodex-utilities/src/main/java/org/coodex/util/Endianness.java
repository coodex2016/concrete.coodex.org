/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

public enum Endianness {
    BIG_ENDIAN(false), LITTLE_ENDIAN(true);
    private final boolean reverse;

    Endianness(boolean reverse) {
        this.reverse = reverse;
    }

    public int readDword(byte[] buf, int offset) {
        byte[] bits = read(buf, offset, 4);
        return toInt(bits, 0);
    }

    public int readWord(byte[] buf, int offset) {
        byte[] bits = read(buf, offset, 2);
        return ((bits[0] & 0xFF) << 8) | (bits[1] & 0xFF);
    }

    public long readLong(byte[] buf, int offset) {
        byte[] bits = read(buf, offset, 8);
        return ((toInt(bits, 0) & 0xFFFFFFFFL) << 32) | (toInt(bits, 4) & 0xFFFFFFFFL);
    }

    public float readFloat(byte[] buf, int offset) {
        return Float.intBitsToFloat(readDword(buf, offset));
    }

    public double readDouble(byte[] buf, int offset) {
        return Double.longBitsToDouble(readLong(buf, offset));
    }

    public byte[] wordToByte(int value) {
        return getBytes(new byte[]{(byte) (((short) value) >>> 8), (byte) value});
    }

    public byte[] dwordToBytes(int value) {
        return getBytes(new byte[]{(byte) (value >>> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value});
    }

    public byte[] longToBytes(long value) {
        return getBytes(new byte[]{(byte) (value >>> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32), (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value});
    }

    public byte[] floatToBytes(float value) {
        return dwordToBytes(Float.floatToIntBits(value));
    }

    public byte[] doubleToBytes(double value) {
        return longToBytes(Double.doubleToLongBits(value));
    }

    private byte[] getBytes(byte[] output) {
        if (reverse) {
            return reverseBytes(output);
        }
        return output;
    }


    private byte[] read(byte[] buf, int offset, int len) {
        if (offset + len > buf.length) {
            throw new ArrayIndexOutOfBoundsException("offset: " + offset + " + len: " + len + " > buf size: " + buf.length);
        }
        byte[] output = new byte[len];
        System.arraycopy(buf, offset, output, 0, len);
        return getBytes(output);
    }

    public static byte[] reverseBytes(byte[] buf) {
        for (int i = 0, len = buf.length, j = len - 1, mid = len / 2; i < mid; i++, j--) {
            byte b = buf[i];
            buf[i] = buf[j];
            buf[j] = b;
        }
        return buf;
    }

    private static int toInt(byte[] bits, int offset) {
        return ((bits[offset] & 0xFF) << 24) | ((bits[offset + 1] & 0xFF) << 16) | ((bits[offset + 2] & 0xFF) << 8) | (bits[offset + 3] & 0xFF);
    }

}
