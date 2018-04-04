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

package org.coodex.concrete.core.messages;

import org.coodex.concrete.client.MessageSubscriber;
import org.coodex.concrete.common.*;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.common.messages.MessageFilter;
import org.coodex.concrete.common.messages.Subscription;
import org.coodex.concrete.core.token.TokenManager;
import org.coodex.concrete.core.token.TokenWrapper;

import static org.coodex.concrete.common.ConcreteContext.SIDE_LOCAL_INVOKE;

public class TokenBaseSubscription<T> extends AbstractSubscription<T> {

    private final String tokenId;
    private final Courier courier;


    public TokenBaseSubscription(String subject) {
        this(subject, null);
    }

    public TokenBaseSubscription(String subject, TokenBaseMessageFilter<T> filter) {
        super(subject, filter);
        ServiceContext context = ConcreteContext.getServiceContext();
        if (context.getSide() == SIDE_LOCAL_INVOKE) {
            tokenId = null;
        } else {
            Token token = TokenWrapper.getInstance();
            if (token == null || !token.isValid()) {
                throw new RuntimeException("invalid token.");
            }
            tokenId = token.getTokenId();
            if (context.getCourier() == null)
                throw new RuntimeException("no courier found.");
        }

        this.courier = context.getCourier();
    }

    @Override
    public MessageFilter<? super T> getFilter() {
        return super.getFilter();
    }

    @Override
    public void onMessage(Message<T> message) {
        if (tokenId != null) {
            Token token = BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(tokenId, false);
            if (token == null || !token.isValid()) {
                if (subscriber != null) {
                    subscriber.cancel();
                }
                return;
            }
            synchronized (token) {
                this.courier.pushTo(message, token);
            }
        } else { // local invoke
            MessageSubscriber.next(message.getSubject(),
                    JSONSerializerFactory.getInstance().toJson(message.getBody())
            );
        }
    }

    @Override
    public boolean isSame(Subscription anotherSubscription) {
        if (!(anotherSubscription instanceof TokenBaseSubscription)) return false;
        TokenBaseSubscription that = (TokenBaseSubscription) anotherSubscription;
        if (tokenId != null ? !tokenId.equals(that.tokenId)
                : that.tokenId != null) return false;
        if (!this.courier.getType().equals(that.courier.getType())) return false;
        return super.isSame(anotherSubscription);
    }
}
