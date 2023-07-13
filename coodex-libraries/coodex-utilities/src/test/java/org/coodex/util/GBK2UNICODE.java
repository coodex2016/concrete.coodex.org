/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class GBK2UNICODE {

//    static class Range{
//        int start, end;
//        List<Integer> values = new ArrayList<>();
//    }

//    static List<Range> splice(Map<Integer, Integer> map, int delta){
//
//    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        StringJoiner joiner = new StringJoiner(",");
        Map<Integer, Integer> gbkUnicodeMap = new HashMap<>();
        Map<Integer, Integer> unicodeGBKMap = new HashMap<>();
        for (int i = 0x8140; i < 0xFE9F; i++) {
            byte ch[] = {(byte) (i >> 8), (byte) (i & 0xFF)};
            int code = new String(ch, "GBK").charAt(0) & 0xFFFF;
            if (code == 65533) code = 0;
            if (code != 0) {
                gbkUnicodeMap.put(i, code);
                unicodeGBKMap.put(code, i);
            }
            joiner.add(String.valueOf(code));
        }
//        System.out.println(joiner);
//        System.out.println(0xFE9F - 0x8140 + 1);
//        System.out.println(new String(Common.base16Decode("D6 D0 CE C4 47 42 4B 31 32 33 B2 E2 CA D4 "),"GBK"));
//
//        String utf8 =new String(Common.base16Decode("E4 B8 AD E6 96 87 47 42 4B E2 80 94 E2 80 94 31 \n" +
//                "        32 33 E6 B5 8B E8 AF 95"), StandardCharsets.UTF_8);
//        System.out.println(utf8);
//        System.out.println(Common.base16Encode(utf8.getBytes("GBK"),16, " "));
//        System.out.println(0xa1aa - 0x8140);
//        System.out.println((char)8212);
        System.out.println(gbkUnicodeMap.size());

        List<Integer> validGBKCodes = gbkUnicodeMap.keySet().stream().sorted().collect(Collectors.toList());
        StringBuilder builder = new StringBuilder();
        StringJoiner blocks = new StringJoiner(", ");
        int hold = 2;
        int start = -1, end = 0;
        int block = 0;
//        for (int i = 0, j = validGBKCodes.size() - 1; i < j; i++) {
//            int a = validGBKCodes.get(i);
//            int b = validGBKCodes.get(i + 1);
//            if (start == -1) start = a;
//            if ((b - a) > hold) {
//                end = a;
//                String blockName = "gbk_chars_0x" + Common.base16Encode(new byte[]{(byte) (start >> 8)});
//                StringJoiner jn = new StringJoiner(",");
//                for (int s = start; s <= end; s++) {
//                    jn.add(gbkUnicodeMap.getOrDefault(s, 0).toString());
//                }
//                builder.append("unsigned short ").append(blockName).append("[] = {").append(jn).append("};\n\n");
//                blocks.add("{" + start + ", " + end + ", " + blockName + "}");
//                System.out.printf("start: %04x, end: %04x, len: %d%n", start, end, end - start + 1);
//                block++;
//                start = -1;
//            }
//        }
//        end = validGBKCodes.get(validGBKCodes.size() - 1);
//        System.out.printf("start: %04x, end: %04x, len: %d%n", start, end, end - start + 1);
//        block++;
//        System.out.println("blocks: " + block + " fe - 81 + 1:" + (0xFE - 0x81 + 1));
//        String blockName = "gbk_chars_0x" + Common.base16Encode(new byte[]{(byte) (start >> 8)});
//        StringJoiner jn = new StringJoiner(",");
//        for (int s = start; s <= end; s++) {
//            jn.add(gbkUnicodeMap.getOrDefault(s, 0).toString());
//        }
//        builder.append("unsigned short ").append(blockName).append("[] = {").append(jn).append("};\n\n");
//        blocks.add("{" + start + ", " + end + ", " + blockName + "}");
//        System.out.println(builder);
//        System.out.println("GBK_CHARS all_gbk_chars[] = {" + blocks + "};");


        List<Integer> validUnicodeCodes = unicodeGBKMap.keySet().stream().sorted().collect(Collectors.toList());
        hold = 1;
        block = 0;
        start = -1;
        StringBuilder charDef = new StringBuilder();
        StringJoiner uni2gbk_CHARS_BLOCK = new StringJoiner(", ");
        for (int i = 0, j = validUnicodeCodes.size() - 1; i < j; i++) {
            int a = validUnicodeCodes.get(i);
            int b = validUnicodeCodes.get(i + 1);
            if (start == -1) start = a;
            if ((b - a) > hold) {
                end = a;
//                System.out.printf("start: %04x, end: %04x, len: %d%n", start, end, end - start + 1);
                block++;
                String tableName = "unicode2gbk_" + block;
                StringJoiner jn = new StringJoiner(",");
                for (int x = start; x <= end; x++) {
                    jn.add(unicodeGBKMap.getOrDefault(x, 0).toString());
                }
                charDef.append("unsigned short ").append(tableName).append("[] = {").append(jn).append("};\n\n");
                uni2gbk_CHARS_BLOCK.add("{" + start + ", " + end + ", " + tableName + "}");
                start = -1;
            }
        }
        block++;
        end = validUnicodeCodes.get(validUnicodeCodes.size() - 1);
        if (start == -1) start = end;
        String tableName = "unicode2gbk_" + block;
        StringJoiner jn = new StringJoiner(",");
        for (int x = start; x <= end; x++) {
            jn.add(unicodeGBKMap.getOrDefault(x, 0).toString());
        }
        charDef.append("unsigned short ").append(tableName).append("[] = {").append(jn).append("}\n\n");
        uni2gbk_CHARS_BLOCK.add("{" + start + ", " + end + ", " + tableName + "}");

        System.out.println(charDef);
        System.out.println("CHARS_BLOCK all_unicode_chars[] = {" + uni2gbk_CHARS_BLOCK + "};");
//        System.out.printf("start: %04x, end: %04x, len: %d%n", start, end, end - start + 1);
        block++;
//        System.out.println("blocks: " + block );
    }
}
