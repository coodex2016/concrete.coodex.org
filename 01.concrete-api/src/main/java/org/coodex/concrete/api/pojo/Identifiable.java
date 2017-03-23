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

package org.coodex.concrete.api.pojo;

/**
 * Created by davidoff shen on 2017-03-23.
 */
public class Identifiable<ID, POJO> {

    private ID id;
    private POJO pojo;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public POJO getPojo() {
        return pojo;
    }

    public void setPojo(POJO pojo) {
        this.pojo = pojo;
    }
}
