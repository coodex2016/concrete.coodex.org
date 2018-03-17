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

package test.org.coodex.concrete.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by davidoff shen on 2017-03-17.
 */
@Entity
@Table(name = "test_table")
public class TestEntity implements Serializable {
    @Id
    private String id;

    @Column(name = "col1", length = 20)
    private String strAttr;

    private Integer intAttr;

    @ElementCollection
    private Set<String> colAttr;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStrAttr() {
        return strAttr;
    }

    public void setStrAttr(String strAttr) {
        this.strAttr = strAttr;
    }

    public Integer getIntAttr() {
        return intAttr;
    }

    public void setIntAttr(Integer intAttr) {
        this.intAttr = intAttr;
    }

    public Set<String> getColAttr() {
        return colAttr;
    }

    public void setColAttr(Set<String> colAttr) {
        this.colAttr = colAttr;
    }
}
