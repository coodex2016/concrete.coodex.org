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

package org.coodex.sharedcache;

import org.coodex.util.SelectableService;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public interface SharedCacheClientFactory extends SelectableService<String> {

    Long DEFAULT_MAX_CACHED_SECONDS = 3600l;

    boolean isAccepted(String driverName);

    SharedCacheClient getClientInstance();

}
