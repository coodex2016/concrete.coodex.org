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
//package org.coodex.concrete.api;
//
//import java.lang.annotation.*;
//
///**
// * Created by davidoff shen on 2017-05-08.
// * <p>
// * 2020-05-15调整定义
// */
//@Target({ElementType.METHOD, ElementType.TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//@Overlay(definition = false)
//@Deprecated
//public @interface LogAtomic {
//
//    String category() default ""; // 默认为serviceClassName
//
//    String subClass() default ""; // 默认未methodName
//
//    @Deprecated
//    String message() default "";
//
//    LoggingType loggingType() default LoggingType.DATA;
//
//    /**
//     * ALWAYS: 一直记录
//     * DATA: 当有数据时进行记录
//     * NO: 不记录
//     */
//    enum LoggingType {
//        ALWAYS, DATA, NO
//    }
//
////    Class<? extends OperationLogger> loggerClass() default OperationLogger.class;
//}
