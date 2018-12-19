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

package org.coodex.util.locks;

import org.coodex.concurrent.locks.AbstractResourceLock;
import org.coodex.concurrent.locks.ResourceId;
import org.coodex.concurrent.locks.ResourceLock;
import org.coodex.concurrent.locks.ResourceLockProvider;

import java.util.HashMap;
import java.util.Map;

public class TestLocalResourceLockProvider implements ResourceLockProvider {

    private static Map<ResourceId, ResourceLock> localLockMap = new HashMap<ResourceId, ResourceLock>(8);

    @Override
    public ResourceLock getLock(ResourceId id) {
        if (!localLockMap.containsKey(id)) {
            synchronized (localLockMap) {
                if (!localLockMap.containsKey(id)) {
                    localLockMap.put(id, new TestLocalResourceLockProvider.LocalResourceLock(id));
                }
            }
        }
        ResourceLock lock = localLockMap.get(id);
        return lock == null ? getLock(id) : lock;
    }


    @Override
    public boolean accept(ResourceId param) {
        return param != null;
    }

    static class LocalResourceLock extends AbstractResourceLock {
//        private boolean allocated = false;

        public LocalResourceLock(ResourceId resourceId) {
            super(resourceId);
        }

        @Override
        protected void alloc() {
//            allocated = true;
        }

        @Override
        protected boolean allocated() {
            return true;
        }

        @Override
        protected void release() {
//            allocated = false;
            if (localLockMap.containsKey(getId())) {
                synchronized (localLockMap) {
                    if (localLockMap.containsKey(getId()))
                        localLockMap.remove(getId());
                }
            }
        }

        @Override
        protected boolean tryAlloc() {
//            allocated = true;
            return true;
        }

        @Override
        protected boolean tryAlloc(long time) {
//            allocated = true;
            return true;
        }
    }
}
