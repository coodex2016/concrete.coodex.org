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

package org.coodex.concrete.message;

import java.util.regex.Pattern;

public class AggregatedCourierPrototypeProvider implements CourierPrototypeProvider {
//    static final String AGGREGATED = "Aggregated::";

    static final Pattern AGGREGATED_PATTERN = Pattern.compile("^\\s*(?i)Aggregated\\s*\\(([\\s,\\w]+)\\)\\s*$");

    @Override
    public Class<? extends CourierPrototype> getPrototype() {
        return AggregatedCourierPrototype.class;
    }

    @Override
    public boolean accept(String param) {
        return param != null &&
                AGGREGATED_PATTERN.matcher(param).matches();
    }

//    public static void main(String[] args) {
//        Pattern pattern = Pattern.compile("^\\s*(?i)Aggregated\\s*\\(([\\s,\\w]+)\\)\\s*$");
//
//        String [] toCheck = {
//                "Aggregated(a)",
//                "aggregated (a, 1, b)",
//                "  Aggregated (a, b, c)  ",
//                "  Aggregated (a, b, c)  Aggregated (a, b, c)",
//                "Aggregated (a, b, c)x"
//        };
//
//        for(String s : toCheck){
//            Matcher matcher = pattern.matcher(s);
//            StringBuilder builder = new StringBuilder(s);
//            boolean matches = matcher.matches();
//            builder.append(" matches: ").append(matches);
//            if(matches){
//                builder.append("\ngroupCount: ").append(matcher.groupCount());
//                for(int i = 0; i <= matcher.groupCount(); i++){
//                    builder.append("\n\tgroup").append(i).append(": ").append(matcher.group(i));
//                }
//            }
//            System.out.println(builder.toString());
//        }
//
//    }
}
