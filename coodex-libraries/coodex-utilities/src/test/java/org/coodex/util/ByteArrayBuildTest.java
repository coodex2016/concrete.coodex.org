///*
// * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.coodex.util;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.nio.charset.StandardCharsets;
//
//public class ByteArrayBuildTest {
//
//    @Test
//    public void test() {
//        BytesBuilder byteArrayBuilder;
//        byteArrayBuilder = new BytesBuilder(Endianness.BIG_ENDIAN, StandardCharsets.UTF_8).appendLong(0xABCDl);
//        Assert.assertEquals("000000000000ABCD", Common.base16Encode(byteArrayBuilder.toByteArray()).toUpperCase());
//
//        byteArrayBuilder = new BytesBuilder().appendString("1234567890").appendCRC(CRC.Algorithm.CRC16_MODBUS);
//        Assert.assertEquals(Common.base16Encode("1234567890".getBytes()) + "0AC2",
//                Common.base16Encode(byteArrayBuilder.toByteArray()).toUpperCase());
//
//        byteArrayBuilder = new BytesBuilder().appendShort(0xABCD);
//        Assert.assertEquals("CDAB", Common.base16Encode(byteArrayBuilder.toByteArray()).toUpperCase());
//
//    }
//}
