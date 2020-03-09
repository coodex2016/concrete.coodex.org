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

package org.coodex.concrete.core;

import java.util.List;
import java.util.Map;

public interface Settings {


    Boolean getBool(String key);

    Integer getInt(String key);

    int getInt(String key, int value);

    void setInt(String key, int value);

    Long getLong(String key);

    long getLong(String key, long value);

    void setLong(String key, long value);

    String getString(String key);

    String getString(String key, String value);

    void setString(String key, String value);

    List<String> getStrList(String key);

    List<String> getStrList(String key, String sp);

    List<String> getStrList(String key, List<String> value);

    List<String> getStrList(String key, String sp, List<String> value);

    void setStrList(String key, List<String> value);

    Map<String, String> allSettings();
}
