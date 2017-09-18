/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.common;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-11-01.
 */
public interface BeanProvider {

    <T> T getBean(Class<T> type);

//    <T> T getBean(Type t);

//    <T> T getBean(String getName);

//    <T> T getBean(Class<T> type, String getName);

    <T> Map<String, T> getBeansOfType(Class<T> type);

}
