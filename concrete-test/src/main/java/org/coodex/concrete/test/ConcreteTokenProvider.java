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

package org.coodex.concrete.test;

import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.junit.runner.Description;

//import java.util.HashMap;
//import java.util.Map;

/**
 * Created by davidoff shen on 2016-09-06.
 */
public class ConcreteTokenProvider {

//    private final static Logger log = LoggerFactory.getLogger(ConcreteTokenProvider.class);

//    private final static TokenManager TOKEN_MANAGER_INSTANCE = getInstance();

//    private static TokenManager getInstance() {
//        try {
//            return BeanServiceLoaderProvider.getBeanProvider().getBean(TokenManager.class);
//        } catch (ConcreteException ex) {
//            log.warn("error occurred: {}. Using LocalTokenManager", ex.getLocalizedMessage());
//            return new LocalTokenManager();
//        }
//    }


//    public static Token getToken(String id) {
//        return TOKEN_MANAGER_INSTANCE.getToken(Common.isBlank(id) ? Common.getUUIDStr() : id, true);
//    }

    public static Token getToken(Description description) {
        TokenID testToken = description.getAnnotation(TokenID.class);
        return TokenWrapper.getToken(testToken == null ? null : testToken.value());
    }

}
