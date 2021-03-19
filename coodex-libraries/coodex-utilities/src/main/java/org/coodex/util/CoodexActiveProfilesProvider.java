/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import java.util.ArrayList;

public class CoodexActiveProfilesProvider implements ActiveProfilesProvider {
    private final Singleton<String[]> activeProfiles = Singleton.with(
            () ->
                    Common.toArray(System.getProperty("coodex.active.profiles", ""), ",", new ArrayList<>())
                            .stream()
                            .filter(s -> !Common.isBlank(s))
                            .toArray(String[]::new)

    );

    @Override
    public String[] getActiveProfiles() {
        return activeProfiles.get();
    }
}
