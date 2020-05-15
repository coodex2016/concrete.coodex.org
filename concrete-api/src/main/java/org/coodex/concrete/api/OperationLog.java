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
//import org.coodex.concrete.common.OperationLogger;
//
//import java.lang.annotation.*;
//
///**
// * 定义操作日志渲染行为
// * <p>
// * Created by davidoff shen on 2017-05-08.
// */
//@Target({ElementType.TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//@Overlay(definition = false)
//@Deprecated
//public @interface OperationLog {
//
//    String category() default "";
//
////    Class<? extends LogFormatter> formatterClass() default LogFormatter.class;
//
////    Class<? extends MessagePatternLoader> patternLoaderClass() default MessagePatternLoader.class;
//
//    Class<? extends OperationLogger> loggerClass() default OperationLogger.class;
//}
