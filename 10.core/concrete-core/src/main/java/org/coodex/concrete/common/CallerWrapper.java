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

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;

public class CallerWrapper implements Caller {

    private static Caller instance = new CallerWrapper();

    private static Caller defaultCaller = new Caller() {
        @Override
        public String getAddress() {
            return "unknown";
        }

        @Override
        public String getClientProvider() {
            return "unknown";
        }
    };

    private CallerWrapper() {
    }

    public static Caller getInstance() {
        return instance;
    }

    private Caller getCaller() {
        ServiceContext serviceContext = getServiceContext();
        if (serviceContext instanceof ContainerContext) {
            return ((ContainerContext) serviceContext).getCaller();
        } else {
            return defaultCaller;
        }
    }

    @Override
    public String getAddress() {
        return getCaller().getAddress();
    }

    @Override
    public String getClientProvider() {
        return getCaller().getClientProvider();
    }

//    @Override
//    public String getAgent() {
//        return getCaller().getAgent();
//    }
}
