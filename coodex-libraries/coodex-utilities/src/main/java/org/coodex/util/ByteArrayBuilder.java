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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class ByteArrayBuilder {
    private final ByteArrayOutputStream byteArrayOutputStream
            = new ByteArrayOutputStream();
    private final Endianness endianness;

    public ByteArrayBuilder() {
        this(Endianness.LITTLE_ENDIAN);
    }

    public ByteArrayBuilder(Endianness endianness) {
        this.endianness = endianness;
    }

    public ByteArrayBuilder append(byte b) {
        byteArrayOutputStream.write(b & 0xff);
        return this;
    }

    public ByteArrayBuilder append(byte[] buf) {
        byteArrayOutputStream.write(buf, 0, buf.length);
        return this;
    }

    public ByteArrayBuilder append(byte[] buf, int off, int len) {
        byteArrayOutputStream.write(buf, off, len);
        return this;
    }

    private byte[] toBytes(long l, int wide, Endianness endianness) {

        byte[] bytes = new byte[wide];
        boolean little = Endianness.LITTLE_ENDIAN.equals(endianness);
        for (int i = 0; i < wide; i++) {
            bytes[little ? i : (wide - i - 1)] = (byte) l;
            l = l >>> 8;
        }
        return bytes;
    }

    public ByteArrayBuilder append(short word) {
        return append(word, endianness);

    }

    public ByteArrayBuilder append(short word, Endianness endianness) {
        return append(toBytes(word & 0xFFFF, 2, endianness));
    }

    public ByteArrayBuilder append(int i) {
        return append(i, endianness);
    }

    public ByteArrayBuilder append(int i, Endianness endianness) {
        return append(toBytes(i & 0xFFFFFFFFL, 4, endianness));
    }

    public ByteArrayBuilder append(long l, Endianness endianness) {
        return append(toBytes(l, 8, endianness));
    }

    public ByteArrayBuilder append(long l) {
        return append(l, endianness);
    }

    public ByteArrayBuilder append(String string) {
        return append(string.getBytes());
    }

    public ByteArrayBuilder append(String string, String charsetName) {
        try {
            return append(string.getBytes(charsetName));
        } catch (UnsupportedEncodingException e) {
            throw Common.rte(e);
        }
    }

    public ByteArrayBuilder append(CRC.Algorithm algorithm) {
        return append(algorithm, endianness);
    }

    public ByteArrayBuilder append(CRC.Algorithm algorithm, int off, int len) {
        return append(algorithm, off, len, endianness);
    }

    public ByteArrayBuilder append(CRC.Algorithm algorithm, Endianness endianness) {
        return append(algorithm, 0, byteArrayOutputStream.size(), endianness);
    }

    public ByteArrayBuilder append(CRC.Algorithm algorithm, int off, int len, Endianness endianness) {

        return append(toBytes(
                CRC.calculateCRC(algorithm, byteArrayOutputStream.toByteArray(), off, len),
                algorithm.getParameters().getWidth() / 8, endianness));
    }

    public byte[] build() {
        return byteArrayOutputStream.toByteArray();
    }

    public enum Endianness {
        BIG_ENDIAN, LITTLE_ENDIAN
    }

}
