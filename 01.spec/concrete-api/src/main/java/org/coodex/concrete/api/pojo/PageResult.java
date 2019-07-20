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
import org.coodex.pojomocker.MockerRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2017-03-21.
 */
public class PageResult<T> extends AbstractPage {
    @Description(name = "总共多少页")
    @SuppressWarnings("deprecation")
    @MockerRef(name = "total")
    private Long total;
    @Description(name = "总共多少条数据")
    @SuppressWarnings("deprecation")
    @MockerRef(name = "count")
    private Long count;
    private List<T> list = new ArrayList<T>();


    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }


    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Description(name = "记录列表")
    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
