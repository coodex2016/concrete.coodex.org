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

package org.coodex.concurrent.locks;


public class LocalResourceLockProvider extends AbstractResourceLockProvider {

    public LocalResourceLockProvider() {
        super();
    }

    @Override
    public boolean accept(ResourceId param) {
        return param != null;
    }

    @Override
    protected AbstractResourceLock buildResourceLock(ResourceId id) {
        return new LocalResourceLock(id);
    }

    static class LocalResourceLock extends AbstractResourceLock {
        private volatile boolean allocated = false;

        public LocalResourceLock(ResourceId resourceId) {
            super(resourceId);
        }

        @Override
        protected void alloc() {
            allocated = true;
        }

        @Override
        protected boolean allocated() {
            return allocated;
        }

        @Override
        protected void release() {
            allocated = false;
        }

        @Override
        protected boolean tryAlloc() {
            allocated = true;
            return true;
        }

        @Override
        protected boolean tryAlloc(long time) {
            allocated = true;
            return true;
        }
    }

}
