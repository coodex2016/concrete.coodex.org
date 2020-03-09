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

package org.coodex.concrete.common;

import org.coodex.util.CRC;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 可分类的账户ID
 */
public class ClassifiableAccountID implements AccountID, Serializable {

    // 格式 类型::id::crc16_MODBUS
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d+::[^ :\\f\\n\\r\\t\\v]+::\\d+$");

    private final Integer category;
    private String id;

    public ClassifiableAccountID(Integer category) {
        this(category, null);
    }

    public ClassifiableAccountID(Integer category, String id) {
        if (category == null)
            throw new NullPointerException("category MUST NOT null.");
        this.category = category;
        this.id = id;
    }

    public static boolean accept(String idStr){
        return idStr != null && ID_PATTERN.matcher(idStr).matches();
    }

    public static ClassifiableAccountID valueOf(String str) {
        if (str == null)
            throw new NullPointerException("ClassifiableAccountID cannot parse from null.");
        if (!ID_PATTERN.matcher(str).matches())
            throw new IllegalArgumentException(str + " is NOT ClassifiableAccountID");
        String[] array = str.split("::");
        ClassifiableAccountID id = new ClassifiableAccountID(Integer.valueOf(array[0]), array[1]);
        if (str.equals(id.serialize()))
            return id;
        throw new IllegalArgumentException(str + " is NOT valid ClassifiableAccountID");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassifiableAccountID)) return false;

        ClassifiableAccountID that = (ClassifiableAccountID) o;

        if (!Objects.equals(category, that.category)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        int result = category != null ? category.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public Integer getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String serialize() {
        String x = String.format("%d::%s", category, id);
        return String.format("%s::%d", x,
                CRC.calculateCRC(CRC.Algorithm.CRC16_MODBUS, x.getBytes(Charset.forName("UTF-8"))));
    }
}
