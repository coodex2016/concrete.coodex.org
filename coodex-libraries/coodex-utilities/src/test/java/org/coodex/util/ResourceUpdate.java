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

package org.coodex.util;

import org.coodex.util.java8.StringJoiner;

import java.io.*;
import java.nio.charset.StandardCharsets;
//import java.util.StringJoiner;

public class ResourceUpdate {

    private static String item_code(File f) {
        //noinspection IOStreamConstructor
        try (InputStream fileInputStream = new FileInputStream(f)) {
            byte[] buf = DigestHelper.digestBuff(fileInputStream, "md5");
            StringJoiner joiner = new StringJoiner(",");
            for (byte b : buf) {
                joiner.add(String.format("0x%02x", b & 0xFF));
            }
            return "{\"" + f.getName() + "\",{" + joiner.toString() + "}}";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean ignored(String name) {
        if (Common.isBlank(name))
            return true;
        for (int i = 0; i < name.length(); i++) {
            switch (name.charAt(i)) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    continue;
                case '.':
                    return true;
                default:
                    return false;
            }

        }
        return true;

    }

    private static String item_list(String path) {
        File[] files = new File(path).listFiles();
        if (files == null)
            return "";
        StringJoiner joiner = new StringJoiner(",");
        for (File file : files) {
            if (file.isDirectory() || ignored(file.getName()))
                continue;
            joiner.add(item_code(file));
        }
        return joiner.toString();
    }

    public static void main(String[] args) {
        if (args.length < 2)
            return;
        String base_path = args[0];
        String target_path = args[1];
        String code = "#include \"resources.h\"\n" +
                "#ifndef SIMPLE_PD\n" +
                "ResourceItem res_font[]  = {" + item_list(base_path + "/fonts/") + "};\n" +
                "ResourceItem res_image[] = {" + item_list(base_path + "/resources/") + "};\n" +
                "#endif\n" +
                "ResourceItem res_voice[] = {" + item_list(base_path + "/voice/female") + "};\n" +
                "\n" +
                "IMPL_RESOURCE_FUNC(font)\n" +
                "IMPL_RESOURCE_FUNC(image)\n" +
                "IMPL_RESOURCE_FUNC(voice)\n";

        System.out.println(code);
        // write file
        //noinspection IOStreamConstructor
        try (OutputStream os = new FileOutputStream(target_path)) {
            os.write(code.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
