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
//package org.coodex.concrete.core;
//
//import org.coodex.concrete.common.MessageFormatter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.text.MessageFormat;
//
///**
// * 基于java.text.MessageFormat  的实现
// * Created by davidoff shen on 2016-09-04.
// */
//@Deprecated
//public class JavaTextFormatMessageFormatter implements MessageFormatter {
//    private final static Logger log = LoggerFactory.getLogger(JavaTextFormatMessageFormatter.class);
//
//    @Override
//    public String format(String pattern, Object... objects) {
//        try {
//            return MessageFormat.format(pattern, objects);
//        } catch (IllegalArgumentException e) {
//            log.warn("illegal argument :{}. {}", pattern, e.getLocalizedMessage());
//            return pattern;
//        }
//    }
//
//    @Override
//    public String getNamespace() {
//        return null;
//    }
//
//}
