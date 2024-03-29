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


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static org.coodex.util.CRC.calculateCRC;

public class CRCTest {

    @Test
    @Tags(@Tag("CRC"))
    public void testCrc() {
        byte[] content = "1234567890".getBytes();
        //CRC-8
        Assertions.assertEquals(0x52, calculateCRC(CRC.Algorithm.CRC8, content));
        Assertions.assertEquals(0x13, calculateCRC(CRC.Algorithm.CRC8_CDMA2000, content));
        Assertions.assertEquals(0xa4, calculateCRC(CRC.Algorithm.CRC8_DARC, content));
        Assertions.assertEquals(0x38, calculateCRC(CRC.Algorithm.CRC8_DVB_S2, content));
        Assertions.assertEquals(0xdb, calculateCRC(CRC.Algorithm.CRC8_EBU, content));
        Assertions.assertEquals(0xb5, calculateCRC(CRC.Algorithm.CRC8_I_CODE, content));
        Assertions.assertEquals(0x07, calculateCRC(CRC.Algorithm.CRC8_ITU, content));
        Assertions.assertEquals(0x4f, calculateCRC(CRC.Algorithm.CRC8_MAXIM, content));
        Assertions.assertEquals(0xa8, calculateCRC(CRC.Algorithm.CRC8_ROHC, content));
        Assertions.assertEquals(0x6e, calculateCRC(CRC.Algorithm.CRC8_WCDMA, content));

        //CRC-16
        Assertions.assertEquals(0x3218, calculateCRC(CRC.Algorithm.CRC16_CCITT_FALSE, content));
        Assertions.assertEquals(0xc57a, calculateCRC(CRC.Algorithm.CRC16_ARC, content));
        Assertions.assertEquals(0x57d8, calculateCRC(CRC.Algorithm.CRC16_AUG_CCITT, content));
        Assertions.assertEquals(0x6aa7, calculateCRC(CRC.Algorithm.CRC16_BUYPASS, content));
        Assertions.assertEquals(0x5cf8, calculateCRC(CRC.Algorithm.CRC16_CDMA2000, content));
        Assertions.assertEquals(0x4ce7, calculateCRC(CRC.Algorithm.CRC16_DDS110, content));
        Assertions.assertEquals(0x96b1, calculateCRC(CRC.Algorithm.CRC16_DECT_R, content));
        Assertions.assertEquals(0x96b0, calculateCRC(CRC.Algorithm.CRC16_DECT_X, content));
        Assertions.assertEquals(0xbc1b, calculateCRC(CRC.Algorithm.CRC16_DNP, content));
        Assertions.assertEquals(0xa943, calculateCRC(CRC.Algorithm.CRC16_EN13757, content));
        Assertions.assertEquals(0xcde7, calculateCRC(CRC.Algorithm.CRC16_GENIBUS, content));
        Assertions.assertEquals(0x3a85, calculateCRC(CRC.Algorithm.CRC16_MAXIM, content));
        Assertions.assertEquals(0xb4ec, calculateCRC(CRC.Algorithm.CRC16_MCRF4XX, content));
        Assertions.assertEquals(0xe76d, calculateCRC(CRC.Algorithm.CRC16_RIELLO, content));
        Assertions.assertEquals(0x90a8, calculateCRC(CRC.Algorithm.CRC16_T10DIF, content));
        Assertions.assertEquals(0x2704, calculateCRC(CRC.Algorithm.CRC16_TELEDISK, content));
        Assertions.assertEquals(0x95a7, calculateCRC(CRC.Algorithm.CRC16_TMS37157, content));
        Assertions.assertEquals(0x3df5, calculateCRC(CRC.Algorithm.CRC16_USB, content));
        Assertions.assertEquals(0x6691, calculateCRC(CRC.Algorithm.CRC_A, content));
        Assertions.assertEquals(0x286b, calculateCRC(CRC.Algorithm.CRC16_KERMIT, content));
        Assertions.assertEquals(0xc20a, calculateCRC(CRC.Algorithm.CRC16_MODBUS, content));
        Assertions.assertEquals(0x4b13, calculateCRC(CRC.Algorithm.CRC16_X25, content));
        Assertions.assertEquals(0xd321, calculateCRC(CRC.Algorithm.CRC16_XMODEM, content));

        CRC crc = new CRC(CRC.Algorithm.CRC16_XMODEM);
        crc.update(/*crcValue,*/ content);
        Assertions.assertEquals(0xd321, crc.finalCRC(/*crcValue*/));

        crc = new CRC(CRC.Algorithm.CRC16_XMODEM);
        for (int i = 0; i < content.length; i++)
            crc.update(new byte[]{content[i]});
        Assertions.assertEquals(0xd321, crc.finalCRC(/*crcValue*/));


        // CRC32
        Assertions.assertEquals(0x261DAEE5l, calculateCRC(CRC.Algorithm.CRC32, content));
        Assertions.assertEquals(0x506853B6l, calculateCRC(CRC.Algorithm.CRC32_BZIP2, content));
        Assertions.assertEquals(0xF3DBD4FEl, calculateCRC(CRC.Algorithm.CRC32C, content));
        Assertions.assertEquals(0x3804C2CBl, calculateCRC(CRC.Algorithm.CRC32D, content));
        Assertions.assertEquals(0xAF97AC49l, calculateCRC(CRC.Algorithm.CRC32_MPEG2, content));
        Assertions.assertEquals(0xC181FD8El, calculateCRC(CRC.Algorithm.CRC32_POSIX, content));
        Assertions.assertEquals(0x10BF7F00l, calculateCRC(CRC.Algorithm.CRC32Q, content));
        Assertions.assertEquals(0xD9E2511Al, calculateCRC(CRC.Algorithm.CRC32_JAMCRC, content));
        Assertions.assertEquals(0x0BE368EBl, calculateCRC(CRC.Algorithm.CRC32_XFER, content));


        content = "a1234567890x".getBytes();
        //CRC-8
        Assertions.assertEquals(0x52, calculateCRC(CRC.Algorithm.CRC8, content, 1, 10));
        Assertions.assertEquals(0x13, calculateCRC(CRC.Algorithm.CRC8_CDMA2000, content, 1, 10));
        Assertions.assertEquals(0xa4, calculateCRC(CRC.Algorithm.CRC8_DARC, content, 1, 10));
        Assertions.assertEquals(0x38, calculateCRC(CRC.Algorithm.CRC8_DVB_S2, content, 1, 10));
        Assertions.assertEquals(0xdb, calculateCRC(CRC.Algorithm.CRC8_EBU, content, 1, 10));
        Assertions.assertEquals(0xb5, calculateCRC(CRC.Algorithm.CRC8_I_CODE, content, 1, 10));
        Assertions.assertEquals(0x07, calculateCRC(CRC.Algorithm.CRC8_ITU, content, 1, 10));
        Assertions.assertEquals(0x4f, calculateCRC(CRC.Algorithm.CRC8_MAXIM, content, 1, 10));
        Assertions.assertEquals(0xa8, calculateCRC(CRC.Algorithm.CRC8_ROHC, content, 1, 10));
        Assertions.assertEquals(0x6e, calculateCRC(CRC.Algorithm.CRC8_WCDMA, content, 1, 10));

        //CRC-16
        Assertions.assertEquals(0x3218, calculateCRC(CRC.Algorithm.CRC16_CCITT_FALSE, content, 1, 10));
        Assertions.assertEquals(0xc57a, calculateCRC(CRC.Algorithm.CRC16_ARC, content, 1, 10));
        Assertions.assertEquals(0x57d8, calculateCRC(CRC.Algorithm.CRC16_AUG_CCITT, content, 1, 10));
        Assertions.assertEquals(0x6aa7, calculateCRC(CRC.Algorithm.CRC16_BUYPASS, content, 1, 10));
        Assertions.assertEquals(0x5cf8, calculateCRC(CRC.Algorithm.CRC16_CDMA2000, content, 1, 10));
        Assertions.assertEquals(0x4ce7, calculateCRC(CRC.Algorithm.CRC16_DDS110, content, 1, 10));
        Assertions.assertEquals(0x96b1, calculateCRC(CRC.Algorithm.CRC16_DECT_R, content, 1, 10));
        Assertions.assertEquals(0x96b0, calculateCRC(CRC.Algorithm.CRC16_DECT_X, content, 1, 10));
        Assertions.assertEquals(0xbc1b, calculateCRC(CRC.Algorithm.CRC16_DNP, content, 1, 10));
        Assertions.assertEquals(0xa943, calculateCRC(CRC.Algorithm.CRC16_EN13757, content, 1, 10));
        Assertions.assertEquals(0xcde7, calculateCRC(CRC.Algorithm.CRC16_GENIBUS, content, 1, 10));
        Assertions.assertEquals(0x3a85, calculateCRC(CRC.Algorithm.CRC16_MAXIM, content, 1, 10));
        Assertions.assertEquals(0xb4ec, calculateCRC(CRC.Algorithm.CRC16_MCRF4XX, content, 1, 10));
        Assertions.assertEquals(0xe76d, calculateCRC(CRC.Algorithm.CRC16_RIELLO, content, 1, 10));
        Assertions.assertEquals(0x90a8, calculateCRC(CRC.Algorithm.CRC16_T10DIF, content, 1, 10));
        Assertions.assertEquals(0x2704, calculateCRC(CRC.Algorithm.CRC16_TELEDISK, content, 1, 10));
        Assertions.assertEquals(0x95a7, calculateCRC(CRC.Algorithm.CRC16_TMS37157, content, 1, 10));
        Assertions.assertEquals(0x3df5, calculateCRC(CRC.Algorithm.CRC16_USB, content, 1, 10));
        Assertions.assertEquals(0x6691, calculateCRC(CRC.Algorithm.CRC_A, content, 1, 10));
        Assertions.assertEquals(0x286b, calculateCRC(CRC.Algorithm.CRC16_KERMIT, content, 1, 10));
        Assertions.assertEquals(0xc20a, calculateCRC(CRC.Algorithm.CRC16_MODBUS, content, 1, 10));
        Assertions.assertEquals(0x4b13, calculateCRC(CRC.Algorithm.CRC16_X25, content, 1, 10));
        Assertions.assertEquals(0xd321, calculateCRC(CRC.Algorithm.CRC16_XMODEM, content, 1, 10));

        // CRC32
        Assertions.assertEquals(0x261DAEE5l, calculateCRC(CRC.Algorithm.CRC32, content, 1, 10));
        Assertions.assertEquals(0x506853B6l, calculateCRC(CRC.Algorithm.CRC32_BZIP2, content, 1, 10));
        Assertions.assertEquals(0xF3DBD4FEl, calculateCRC(CRC.Algorithm.CRC32C, content, 1, 10));
        Assertions.assertEquals(0x3804C2CBl, calculateCRC(CRC.Algorithm.CRC32D, content, 1, 10));
        Assertions.assertEquals(0xAF97AC49l, calculateCRC(CRC.Algorithm.CRC32_MPEG2, content, 1, 10));
        Assertions.assertEquals(0xC181FD8El, calculateCRC(CRC.Algorithm.CRC32_POSIX, content, 1, 10));
        Assertions.assertEquals(0x10BF7F00l, calculateCRC(CRC.Algorithm.CRC32Q, content, 1, 10));
        Assertions.assertEquals(0xD9E2511Al, calculateCRC(CRC.Algorithm.CRC32_JAMCRC, content, 1, 10));
        Assertions.assertEquals(0x0BE368EBl, calculateCRC(CRC.Algorithm.CRC32_XFER, content, 1, 10));
    }
}
