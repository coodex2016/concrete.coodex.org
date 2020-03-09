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

package org.coodex.commons.jpa.springdata;

import org.coodex.concrete.api.pojo.PageRequest;
import org.coodex.concrete.api.pojo.PageResult;
import org.coodex.copier.Copier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;

import static org.springframework.data.domain.PageRequest.of;

/**
 * Created by davidoff shen on 2017-07-13.
 */
public class PageHelper {

    public static Pageable getPageable(PageRequest pageRequest) {
        return getPageable(pageRequest, null);
    }


    public static Pageable getPageable(PageRequest pageRequest, Sort sort) {
        int pageNo = pageRequest.getNum() != null && pageRequest.getNum() > 0 ? pageRequest.getNum().intValue() - 1 : 0;
        return of(pageNo, pageRequest.getPageSize(), sort);
    }

    public static <SRC, TARGET> PageResult<TARGET> copy(Page<SRC> srcPage, Copier<SRC, TARGET> copier) {
        PageResult<TARGET> result = new PageResult<>();
        result.setCount(srcPage.getTotalElements());
        result.setTotal((long) srcPage.getTotalPages());
        result.setNum((long) srcPage.getNumber() + 1);
        result.setPageSize(srcPage.getSize());
        result.setList(new ArrayList<>());
//        for (SRC src : srcPage.getContent()) {
//            result.getList().add(copier.copy(src));
//        }
        srcPage.get().forEachOrdered((src) -> result.getList().add(copier.copy(src)));
        return result;
    }
}
