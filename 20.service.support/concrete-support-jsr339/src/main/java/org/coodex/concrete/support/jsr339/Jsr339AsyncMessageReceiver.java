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

package org.coodex.concrete.support.jsr339;

import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.jaxrs.AbstractJAXRSResource;
import org.coodex.concrete.jaxrs.AsyncMessageReceiver;
import org.coodex.concrete.jaxrs.JaxRSCourier;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Singleton;

import javax.ws.rs.container.AsyncResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Jsr339AsyncMessageReceiver extends AsyncMessageReceiver {
    private final AsyncResponse asyncResponse;
    private final AbstractJAXRSResource.ResponseBuilder builder;
    private Future future;

    public Jsr339AsyncMessageReceiver(AsyncResponse asyncResponse, long timeOut, final AbstractJAXRSResource.ResponseBuilder builder) {
        this.asyncResponse = asyncResponse;
        this.builder = builder;
        setTokenId(builder.getTokenId());
        final Jsr339AsyncMessageReceiver getter = this;
        this.future = getScheduledExecutorService().schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    JaxRSCourier.deregister(getter);
                    getter.asyncResponse.resume(builder.build(new AbstractJAXRSResource.RunWithToken() {
                        @Override
                        public Object runWithToken(Token token) {
                            return new ArrayList<Message>();
                        }
                    }));
                } finally {
                    future = null;
                }
            }
        }, timeOut, TimeUnit.MILLISECONDS);
    }

//    private static ScheduledExecutorService scheduledExecutorService;

    private static Singleton<ScheduledExecutorService> scheduledExecutorService =
            new Singleton<ScheduledExecutorService>(new Singleton.Builder<ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService build() {
                    return ExecutorsHelper.newScheduledThreadPool(1);
                }
            });

    private static ScheduledExecutorService getScheduledExecutorService() {
//        synchronized (Jsr339AsyncMessageReceiver.class) {
//            if (scheduledExecutorService == null) {
//                scheduledExecutorService = ExecutorsHelper.newScheduledThreadPool(1);
//            }
//        }
//        return scheduledExecutorService;
        return scheduledExecutorService.getInstance();
    }

    @Override
    public void resume(final List<Message> messages) {
        if (this.future != null) {
            try {
                this.future.cancel(true);
            } catch (Throwable throwable) {
            }
            this.future = null;

            asyncResponse.resume(builder.build(new AbstractJAXRSResource.RunWithToken() {
                @Override
                public Object runWithToken(Token token) {
                    return messages;
                }
            }));
        }
    }

}
