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

package test.org.coodex.mock.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CharSetGenerator {

    public static void main(String[] args) throws IOException {

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                CharSetGenerator.class.getResourceAsStream("/charsets.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() < 5) continue;

            String newLine = parseLine(line);
            if (newLine != null)
                System.out.println(newLine);
        }

    }

    private static String parseLine(String line) {
        String[] parts = line.toUpperCase().split("\\\t");
        String range = parts[0];
        // utf-16
        if(range.startsWith("D800") || range.startsWith("DC00")) return null;
        int quote = range.indexOf('[');
        if(quote > 0){
            range = range.substring(0,quote);
        }
        String[] rangeX = range.split("-");
        StringBuilder builder = new StringBuilder();
        builder.append("/**\n * ").append(parts[1]).append("\n */\n")
                .append(parts[2].replace(' ', '_').replace('-', '_').replace('\'', '_'))
                .append("(0x").append(rangeX[0]).append(", 0x")
                .append(rangeX[1]).append("),");
        return builder.toString();
    }
}
