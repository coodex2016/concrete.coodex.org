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
import org.coodex.util.SingletonMap;
import org.coodex.util.StringKeySingletonMap;

/**
 * Created by davidoff shen on 2017-04-24.
 */
public class RSAPenFactory implements IronPenFactory {
    private static final SingletonMap<String, RSAPen> RSA_PEN_SINGLETON_MAP = new StringKeySingletonMap<>(
            RSAPen::new
    );
    @Override
    public IronPen getIronPen(String paperName) {
        return RSA_PEN_SINGLETON_MAP.get(paperName);
    }
//
//    @Override
//    public IronPen getClientSidePen(String paperName) {
//        return new RSAPen(paperName);
//    }

    @Override
    public boolean accept(String param) {
        return param != null && param.toUpperCase().endsWith("RSA");
    }
}
