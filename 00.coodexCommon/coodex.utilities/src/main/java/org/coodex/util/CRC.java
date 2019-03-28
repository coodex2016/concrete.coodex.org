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

// Copyright 2016, S&K Software Development Ltd.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Package crc implements generic CRC calculations up to 64 bits wide.
// It aims to be fairly complete, allowing users to match pretty much
// any CRC algorithm used in the wild by choosing appropriate Parameters.
// And it's also fairly fast for everyday use.
//
// This package has been largely inspired by Ross Williams' 1993 paper "A Painless Guide to CRC Error Detection Algorithms".
// A good list of parameter sets for various CRC algorithms can be found at http://reveng.sourceforge.net/crc-catalogue/.

// https://github.com/snksoft/java-crc

/**
 * This class provides utility functions for CRC calculation using either canonical straight forward approach
 * or using "fast" table-driven implementation. Note, that even though table-driven implementation is much faster
 * for processing large amounts of data and is commonly referred as fast algorithm, sometimes it might be quicker to
 * calculate CRC using canonical algorithm then to prepare the table for table-driven implementation.
 *
 * <p>
 * Using src is easy. Here is an example of calculating CCITT crc in one call using canonical approach.
 * <pre>
 * {@code
 * String data = "123456789";
 * long ccittCrc = CRC.calculateCRC(CRC.Parameters.CCITT, data.getBytes());
 * System.out.printf("CRC is 0x%04X\n", ccittCrc); // prints "CRC is 0x29B1"
 * }
 * </pre>
 * <p>
 * For larger data, table driven implementation is faster. Here is how to use it.
 * <pre>
 * {@code
 * String data = "123456789";
 * CRC tableDriven = new CRC(CRC.Parameters.XMODEM);
 * long xmodemCrc = tableDriven.calculateCRC(data.getBytes());
 * System.out.printf("CRC is 0x%04X\n", xmodemCrc); // prints "CRC is 0x31C3"
 * }
 * </pre>
 * <p>
 * You can also reuse CRC object instance for another crc calculation.
 * <p>
 * Given that the only state for a CRC calculation is the "intermediate value"
 * and it is stored in your code, you can even use same CRC instance to calculate CRC
 * of multiple data sets in parallel.
 * And if data is too big, you may feed it in chunks
 * <pre>
 * {@code
 * long curValue = tableDriven.init(); // initialize intermediate value
 * curValue = tableDriven.update(curValue, "123456789".getBytes()); // feed first chunk
 * curValue = tableDriven.update(curValue, "01234567890".getBytes()); // feed next chunk
 * long xmodemCrc2 = tableDriven.finalCRC(curValue); // gets CRC of whole data ("12345678901234567890")
 * System.out.printf("CRC is 0x%04X\n", xmodemCrc2); // prints "CRC is 0x2C89"
 * }
 * </pre>
 */
public class CRC {
    private Parameters crcParams;
    private long initValue;
    private long curValue;
    private long[] crctable;
    private long mask;

    /**
     * Constructs a new CRC processor for table based CRC calculations.
     * Underneath, it just calls finalCRC() method.
     *
     * @param algorithm CRC algorithm parameters
     * @throws RuntimeException if CRC sum width is not divisible by 8
     */
    public CRC(Algorithm algorithm) {
        this(algorithm.parameters);
    }

    public CRC(Parameters crcParams) {
        this.crcParams = new Parameters(crcParams);

        initValue = (crcParams.reflectIn) ? reflect(crcParams.init, crcParams.width) : crcParams.init;
        this.mask = ((crcParams.width >= 64) ? 0 : (1L << crcParams.width)) - 1;
        this.crctable = new long[256];

        byte[] tmp = new byte[1];

        Parameters tableParams = new Parameters(crcParams);

        tableParams.init = 0;
        tableParams.reflectOut = tableParams.reflectIn;
        tableParams.finalXor = 0;
        for (int i = 0; i < 256; i++) {
            tmp[0] = (byte) i;
            crctable[i] = CRC.calculateCRC(crcParams, tmp);
        }
        curValue = initValue;
    }


    /**
     * Reverses order of last count bits.
     *
     * @param in    value wich bits need to be reversed
     * @param count indicates how many bits be rearranged
     * @return the value with specified bits order reversed
     */
    private static long reflect(long in, int count) {
        long ret = in;
        for (int idx = 0; idx < count; idx++) {
            long srcbit = 1L << idx;
            long dstbit = 1L << (count - idx - 1);
            if ((in & srcbit) != 0) {
                ret |= dstbit;
            } else {
                ret = ret & (~dstbit);
            }
        }
        return ret;
    }

    public static long calculateCRC(Algorithm algorithm, byte[] data) {
        return calculateCRC(algorithm, data, 0, data.length);
    }

    public static long calculateCRC(Algorithm algorithm, byte[] data, int offset, int length) {
        return calculateCRC(algorithm.parameters, data, offset, length);
    }

    public static long calculateCRC(Parameters crcParams, byte[] data) {
        return calculateCRC(crcParams, data, 0, data.length);
    }

    /**
     * This method implements simple straight forward bit by bit calculation.
     * It is relatively slow for large amounts of data, but does not require
     * any preparation steps. As a result, it might be faster in some cases
     * then building a table required for faster calculation.
     * <p>
     * Note: this implementation follows section 8 ("A Straightforward CRC Implementation")
     * of Ross N. Williams paper as even though final/sample implementation of this algorithm
     * provided near the end of that paper (and followed by most other implementations)
     * is a bit faster, it does not work for polynomials shorter then 8 bits.
     *
     * @param crcParams CRC algorithm parameters
     * @param data      data for the CRC calculation
     * @return the CRC value of the data provided
     */
    public static long calculateCRC(Parameters crcParams, byte[] data, int offset, int length) {
        long curValue = crcParams.init;
        long topBit = 1L << (crcParams.width - 1);
        long mask = (topBit << 1) - 1;

        for (int i = offset, upper = Math.min(offset + length, data.length); i < upper; i++) {
            long curByte = ((long) (data[i])) & 0x00FFL;
            if (crcParams.reflectIn) {
                curByte = reflect(curByte, 8);
            }

            for (int j = 0x80; j != 0; j >>= 1) {
                long bit = curValue & topBit;
                curValue <<= 1;

                if ((curByte & j) != 0) {
                    bit ^= topBit;
                }

                if (bit != 0) {
                    curValue ^= crcParams.polynomial;
                }
            }

        }

        if (crcParams.reflectOut) {
            curValue = reflect(curValue, crcParams.width);
        }

        curValue = curValue ^ crcParams.finalXor;

        return curValue & mask;
    }

    /**
     * Returns initial value for this CRC intermediate value
     * This method is used when starting a new iterative CRC calculation (using init, update
     * and finalCRC methods, possibly supplying data in chunks).
     *
     * @return initial value for this CRC intermediate value
     */
    public long init() {
        return initValue;
    }

    /**
     * This method is used to feed data when performing iterative CRC calculation (using init, update
     * and finalCRC methods, possibly supplying data in chunks). It can be called multiple times per
     * CRC calculation to feed data to be processed in chunks.
     *
     * @param chunk    data chunk to b processed by this call
     * @param offset   is 0-based offset of the data to be processed in the array supplied
     * @param length   indicates number of bytes to be processed.
     * @return updated intermediate value for this CRC
     */
    public long update(/*long curValue,*/ byte[] chunk, int offset, int length) {
//        long curValue = currentValue;
        if (crcParams.reflectIn) {
            for (int i = 0; i < length; i++) {
                byte v = chunk[offset + i];
                curValue = crctable[(((byte) curValue) ^ v) & 0x00FF] ^ (curValue >>> 8);
            }
        } else if (crcParams.width < 8) {
            for (int i = 0; i < length; i++) {
                byte v = chunk[offset + i];
                curValue = crctable[((((byte) (curValue << (8 - crcParams.width))) ^ v) & 0xFF)] ^ (curValue << 8);
            }
        } else {
            for (int i = 0; i < length; i++) {
                byte v = chunk[offset + i];
                curValue = crctable[((((byte) (curValue >>> (crcParams.width - 8))) ^ v) & 0xFF)] ^ (curValue << 8);
            }
        }

        return curValue;
    }

    /**
     * A convenience method for feeding a complete byte array of data.
     *
     * @param chunk    data chunk to b processed by this call
     * @return updated intermediate value for this CRC
     */
    public long update(/*long curValue,*/ byte[] chunk) {
        return update(/*curValue,*/ chunk, 0, chunk.length);
    }

    /**
     * This method should be called to retrieve actual CRC for the data processed so far.
     *
     * @return calculated CRC
     */
    public long finalCRC(/*long curValue*/) {
//        long ret = curValue;
        if (crcParams.reflectOut != crcParams.reflectIn) {
            curValue = reflect(curValue, crcParams.width);
        }
        curValue =  (curValue ^ crcParams.finalXor) & mask;
        return curValue;
    }

//    /**
//     * A convenience method allowing to calculate CRC in one call.
//     *
//     * @param data is data to calculate CRC on
//     * @return calculated CRC
//     */
//    public long calculateCRC(byte[] data) {
//        long crc = init();
//        crc = update(crc, data);
//        return finalCRC(crc);
//    }

    /**
     * Is a convenience method to spare end users from explicit type conversion every time this package is used.
     * Underneath, it just calls finalCRC() method.
     *
     * @return the final CRC value
     * @throws RuntimeException if crc being calculated is not 8-bit
     */
    public byte finalCRC8(/*long curValue*/) {
        if (crcParams.width != 8)
            throw new RuntimeException("CRC width mismatch");
        return (byte) finalCRC(/*curValue*/);
    }

    /**
     * Is a convenience method to spare end users from explicit type conversion every time this package is used.
     * Underneath, it just calls finalCRC() method.
     *
     * @return the final CRC value
     * @throws RuntimeException if crc being calculated is not 16-bit
     */
    public short finalCRC16(/*long curValue*/) {
        if (crcParams.width != 16)
            throw new RuntimeException("CRC width mismatch");
        return (short) finalCRC(/*curValue*/);
    }

    /**
     * Is a convenience method to spare end users from explicit type conversion every time this package is used.
     * Underneath, it just calls finalCRC() method.
     *
     * @return the final CRC value
     * @throws RuntimeException if crc being calculated is not 32-bit
     */
    public int finalCRC32(/*long curValue*/) {
        if (crcParams.width != 32)
            throw new RuntimeException("CRC width mismatch");
        return (int) finalCRC(/*curValue*/);
    }

    /**
     * Parameters represents set of parameters defining a particular CRC algorithm.
     * <p>
     * https://crccalc.com/
     */
    public enum Algorithm {
        //CRC-8
        CRC8(new Parameters(8, 0x07, 0x00, false, false, 0x0)),
        CRC8_CDMA2000(new Parameters(8, 0x9b, 0xff, false, false, 0x0)),
        CRC8_DARC(new Parameters(8, 0X39, 0X00, true, true, 0x0)),
        CRC8_DVB_S2(new Parameters(8, 0xd5, 0x00, false, false, 0x0)),
        CRC8_EBU(new Parameters(8, 0x1d, 0xff, true, true, 0x0)),
        CRC8_I_CODE(new Parameters(8, 0x1d, 0xfd, false, false, 0x0)),
        CRC8_ITU(new Parameters(8, 0x07, 0x00, false, false, 0x55)),
        CRC8_MAXIM(new Parameters(8, 0X31, 0X00, true, true, 0x00)),
        CRC8_ROHC(new Parameters(8, 0x07, 0xff, true, true, 0x00)),
        CRC8_WCDMA(new Parameters(8, 0X9B, 0X00, true, true, 0x00)),
        // CRC-16
        CRC16_CCITT_FALSE(new Parameters(16, 0x1021, 0x00FFFF, false, false, 0x0)),
        CRC16_ARC(new Parameters(16, 0x8005, 0x0000, true, true, 0x0)),
        CRC16_AUG_CCITT(new Parameters(16, 0x1021, 0x1D0F, false, false, 0x0)),
        CRC16_BUYPASS(new Parameters(16, 0x8005, 0x0000, false, false, 0x0)),
        CRC16_CDMA2000(new Parameters(16, 0xC867, 0xFFFF, false, false, 0x0)),
        CRC16_DDS110(new Parameters(16, 0x8005, 0x800d, false, false, 0x0)),
        CRC16_DECT_R(new Parameters(16, 0x0589, 0x0000, false, false, 0x0001)),
        CRC16_DECT_X(new Parameters(16, 0x0589, 0x0000, false, false, 0x0)),
        CRC16_DNP(new Parameters(16, 0x3d65, 0x0000, true, true, 0xffff)),
        CRC16_EN13757(new Parameters(16, 0x3d65, 0x0000, false, false, 0xffff)),
        CRC16_GENIBUS(new Parameters(16, 0x1021, 0x00FFFF, false, false, 0xffff)),
        CRC16_MAXIM(new Parameters(16, 0x8005, 0x0000, true, true, 0xffff)),
        CRC16_MCRF4XX(new Parameters(16, 0x1021, 0x00FFFF, true, true, 0x0)),
        CRC16_RIELLO(new Parameters(16, 0x1021, 0x00B2AA, true, true, 0x0)),
        CRC16_T10DIF(new Parameters(16, 0x8bb7, 0x0000, false, false, 0x0000)),
        CRC16_TELEDISK(new Parameters(16, 0xa097, 0x0000, false, false, 0x0000)),
        CRC16_TMS37157(new Parameters(16, 0X1021, 0X89EC, true, true, 0x0000)),
        CRC16_USB(new Parameters(16, 0X8005, 0xffff, true, true, 0xffff)),
        CRC_A(new Parameters(16, 0x1021, 0xc6c6, true, true, 0x0)),
        CRC16_KERMIT(new Parameters(16, 0x1021, 0x0000, true, true, 0x0)),
        CRC16_MODBUS(new Parameters(16, 0X8005, 0XFFFF, true, true, 0x0)),
        CRC16_X25(new Parameters(16, 0x1021, 0xffff, true, true, 0xffff)),
        CRC16_XMODEM(new Parameters(16, 0x1021, 0x0000, false, false, 0x0000)),
        //CRC32
        CRC32(new Parameters(32, 0x04C11DB7, 0xFFFFFFFF, true, true, 0xFFFFFFFF)),
        CRC32_BZIP2(new Parameters(32, 0x04C11DB7, 0xFFFFFFFF, false, false, 0xFFFFFFFF)),
        CRC32C(new Parameters(32, 0x1EDC6F41, 0xFFFFFFFF, true, true, 0xFFFFFFFF)),
        CRC32D(new Parameters(32, 0xA833982B, 0xFFFFFFFF, true, true, 0xFFFFFFFF)),
        CRC32_MPEG2(new Parameters(32, 0x04C11DB7, 0xFFFFFFFF, false, false, 0x00000000)),
        CRC32_POSIX(new Parameters(32, 0x04C11DB7, 0x00000000, false, false, 0xFFFFFFFF)),
        CRC32Q(new Parameters(32, 0x814141AB, 0x00000000, false, false, 0x00000000)),
        CRC32_JAMCRC(new Parameters(32, 0x04C11DB7, 0xFFFFFFFF, true, true, 0x00000000)),
        CRC32_XFER(new Parameters(32, 0x000000AF, 0x00000000, false, false, 0x00000000));


        private Parameters parameters;

        Algorithm(Parameters parameters) {
            this.parameters = parameters;
        }

        public Parameters getParameters() {
            return parameters;
        }
    }

    public static class Parameters {
        private int width;   // Width of the CRC expressed in bits
        private long polynomial; // Polynomial used in this CRC calculation
        private boolean reflectIn;   // Refin indicates whether input bytes should be reflected
        private boolean reflectOut;   // Refout indicates whether input bytes should be reflected
        private long init; // Init is initial value for CRC calculation
        private long finalXor; // Xor is a value for final xor to be applied before returning result

        public Parameters(int width, long polynomial, long init, boolean reflectIn, boolean reflectOut, long finalXor) {

            this.width = width;
            this.polynomial = polynomial;
            this.reflectIn = reflectIn;
            this.reflectOut = reflectOut;
            this.init = init;
            this.finalXor = finalXor;
        }


        Parameters(Parameters orig) {
            width = orig.width;
            polynomial = orig.polynomial;
            reflectIn = orig.reflectIn;
            reflectOut = orig.reflectOut;
            init = orig.init;
            finalXor = orig.finalXor;
        }


        public int getWidth() {
            return width;
        }

        public long getPolynomial() {
            return polynomial;
        }

        public boolean isReflectIn() {
            return reflectIn;
        }

        public boolean isReflectOut() {
            return reflectOut;
        }

        public long getInit() {
            return init;
        }

        public long getFinalXor() {
            return finalXor;
        }
    }

}