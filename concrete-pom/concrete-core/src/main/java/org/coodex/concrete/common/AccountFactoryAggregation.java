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

package org.coodex.concrete.common;

import org.coodex.util.LazySelectableServiceLoader;

import static org.coodex.util.Common.cast;

/**
 * Created by davidoff shen on 2017-04-27.
 */
public class AccountFactoryAggregation<ID extends AccountID> implements AccountFactory<ID> {

    private final static LazySelectableServiceLoader<AccountID, SelectableAccountFactory<AccountID>>
            ACCOUNT_FACTORY_LOADER = new LazySelectableServiceLoader<AccountID, SelectableAccountFactory<AccountID>>() {
    };


    @Override
    public Account<ID> getAccountByID(ID id) {
        return cast(ACCOUNT_FACTORY_LOADER.select(id).getAccountByID(id));
    }
}
