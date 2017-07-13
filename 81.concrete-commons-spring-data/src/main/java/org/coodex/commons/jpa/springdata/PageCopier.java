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

package org.coodex.commons.jpa.springdata;

import org.coodex.concrete.api.pojo.PageResult;
import org.coodex.concrete.common.Copier;
import org.springframework.data.domain.Page;

/**
 * Created by davidoff shen on 2017-03-21.
 */
public class PageCopier {

    @Deprecated
    public static <SRC, TARGET> PageResult<TARGET> copy(Page<SRC> srcPage, Copier<SRC, TARGET> copier) {
//        PageResult<TARGET> result = new PageResult<TARGET>();
//        result.setCount(srcPage.getTotalElements());
//        result.setTotal((long) srcPage.getTotalPages());
//        result.setNum((long) srcPage.getNumber());
//        result.setPageSize(srcPage.getSize());
//        result.setList(new ArrayList<TARGET>());
//        for (SRC src : srcPage.getContent()) {
//            result.getList().add(copier.copy(src));
//        }
//        return result;
        return PageHelper.copy(srcPage, copier);
    }
}
