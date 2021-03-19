/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CommonTest {

    private static void base16(byte[] bytes1) {

//        System.out.println(Common.base16Encode(bytes1));
//        System.out.println(Common.base16Encode(bytes1, 16, " "));
//        System.out.println(Common.base16Encode(bytes1, line -> line + 1, " "));
//        System.out.println(Common.base16Encode(bytes1, line -> {
//            switch (line) {
//                case 0:
//                    return 8;
//                case 1:
//                case 2:
//                case 3:
//                    return 16;
//                default:
//                    return 10;
//            }
//        }, " "));
    }

    public static void main(String[] args) throws IOException {

        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        System.out.println(Common.base16Encode(bytes));

        System.out.println(Common.base16Encode(bytes,16/*每行16个字节*/," "/*每行中列于列之间使用空格隔开*/));

        System.out.println(Common.base16Encode(bytes,line->line + 1/*每行显示数量为行数+1,行号从0开始*/," "/*每行中列于列之间使用空格隔开*/));

        System.out.println(Common.base16Encode(bytes,16/*从下标为16的元素开始*/,8/*编码8个字节*/));

        System.out.println(Common.base16Encode(bytes,16/*从下标为16的元素开始*/,8/*编码8个字节*/,4/*每行4列*/," "));

        System.out.println(Common.base16Encode(bytes,16/*从下标为16的元素开始*/,10/*编码10个字节*/, line->line+1, " "));



//        System.out.println(Common.base16Encode(bytes2));

//        System.out.println(Common.FILE_SEPARATOR);
//        System.out.println(Common.PATH_SEPARATOR);
//        System.out.println(Common.DEFAULT_TIME_FORMAT);
//        System.out.println(Common.sameString("1","2"));
    }

    private static byte[] nextLine(byte[] lastLine) {
        byte[] result = new byte[lastLine.length + 1];
        result[0] = 1;
        result[lastLine.length] = 1;
        for (int i = 1; i < lastLine.length; i++) {
            int x = (lastLine[i - 1] & 0xFF) + (lastLine[i] & 0xFF);
            if (x > 0xFF) return null;
            result[i] = (byte) x;
        }
        return result;
    }

}
