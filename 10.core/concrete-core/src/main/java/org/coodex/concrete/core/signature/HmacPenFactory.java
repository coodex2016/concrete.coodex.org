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

package org.coodex.concrete.core.signature;

import org.coodex.concrete.common.IronPen;
import org.coodex.concrete.common.IronPenFactory;

/**
 * Created by davidoff shen on 2017-04-21.
 */
public class HmacPenFactory implements IronPenFactory {


    @Override
    public IronPen getIronPen(String paperName) {
        return new HmacPen(paperName);
    }
//
//    @Override
//    public IronPen getClientSidePen(String paperName) {
//        return new HmacPen(paperName);
//    }


    @Override
    public boolean accept(String param) {
        return param != null && param.toUpperCase().startsWith("HMAC");
    }
}
