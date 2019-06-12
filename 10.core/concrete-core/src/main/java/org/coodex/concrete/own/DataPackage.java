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

package org.coodex.concrete.own;

import org.coodex.util.Common;

import java.util.HashMap;
import java.util.Map;

public class DataPackage<T> {

    private T content;
    private Map<String, String> subjoin = new HashMap<String, String>();
    private String msgId = Common.getUUIDStr();
    private String concreteTokenId;

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public Map<String, String> getSubjoin() {
        return subjoin;
    }

    public void setSubjoin(Map<String, String> subjoin) {
        this.subjoin = subjoin;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getConcreteTokenId() {
        return concreteTokenId;
    }

    public void setConcreteTokenId(String concreteTokenId) {
        this.concreteTokenId = concreteTokenId;
    }
}
