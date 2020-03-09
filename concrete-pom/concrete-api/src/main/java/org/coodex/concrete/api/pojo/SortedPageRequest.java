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

package org.coodex.concrete.api.pojo;

import org.coodex.concrete.api.Description;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2017-03-21.
 */
public class SortedPageRequest<T> extends PageRequest<T> {
    @Description(name = "排序信息")
    private List<Sorted> sort = new ArrayList<Sorted>();

    public List<Sorted> getSort() {
        return sort;
    }

    public void setSort(List<Sorted> sort) {
        this.sort = sort;
    }
}
