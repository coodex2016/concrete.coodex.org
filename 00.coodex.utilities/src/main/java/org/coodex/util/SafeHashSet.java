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

package org.coodex.util;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by davidoff shen on 2017-05-19.
 */
public class SafeHashSet<E> extends HashSet<E> {
    public SafeHashSet() {
    }

    public SafeHashSet(Collection<? extends E> c) {
        addAll(c);
    }

    public SafeHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public SafeHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return c == null ? false : super.addAll(c);
    }
}
