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

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import static org.coodex.util.Common.toBytes;

@SuppressWarnings("rawtypes")
public abstract class AbstractBytesBuilder<T extends AbstractBytesBuilder> {

    private final ByteArrayOutputStream byteArrayOutputStream
            = new ByteArrayOutputStream();
    private final Endianness endianness;
    private final Charset charset;


    public AbstractBytesBuilder(Endianness endianness, Charset charset) {
        this.endianness = endianness;
        this.charset = charset;
    }

    protected T appendInt64(long l, int wide) {
        return append(toBytes(l, wide, endianness));
    }


    public T appendByte(byte b) {
        return appendByte(b & 0xFF);
    }

    public T appendByte(int b) {
        byteArrayOutputStream.write(b);
        return getThis();
    }

    public T append(byte[] buf) {
        return append(buf, 0, buf.length);
    }

    public T append(byte[] buf, int off, int len) {
        byteArrayOutputStream.write(buf, off, len);
        return getThis();
    }

    public T appendShort(short word) {
        return appendShort(word & 0xFFFF);
    }

    public T appendShort(int word) {
        return appendInt64(word & 0xFFFFFFFFL, 2);
    }

    public T appendInt(int i) {
        return appendInt64(i & 0xFFFFFFFFL, 4);
    }

    public T appendLong(long l) {
        return appendInt64(l, 8);
    }

    public T appendFloat(float f) {
        return appendInt(Float.floatToIntBits(f));
    }

    public T appendDouble(double d) {
        return appendLong(Double.doubleToLongBits(d));
    }

    public T appendString(String s) {
        return appendString(s, charset);
    }

    public T appendString(String s, String charsetName) {
        return appendString(s, Charset.forName(charsetName));
    }

    public T appendString(String s, Charset charset) {
        return append(s.getBytes(charset));
    }

    public T appendCRC(CRC.Algorithm algorithm) {
        return appendCRC(algorithm, endianness);
    }

    public T appendCRC(CRC.Algorithm algorithm, int off, int len) {
        return appendCRC(algorithm, off, len, endianness);
    }

    public T appendCRC(CRC.Algorithm algorithm, Endianness endianness) {
        return appendCRC(algorithm, 0, byteArrayOutputStream.size(), endianness);
    }

    public T appendCRC(CRC.Algorithm algorithm, int off, int len, Endianness endianness) {

        return append(toBytes(
                CRC.calculateCRC(algorithm, byteArrayOutputStream.toByteArray(), off, len),
                algorithm.getParameters().getWidth() / 8, endianness));
    }

    protected abstract T getThis();

    public byte[] toByteArray() {
        return byteArrayOutputStream.toByteArray();
    }
}
