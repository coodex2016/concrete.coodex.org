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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.concrete.core.intercept.annotations.TestContext;
import org.coodex.concrete.message.*;
import org.coodex.util.Common;
import org.coodex.util.GenericTypeHelper;
import org.coodex.util.Singleton;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Type;

import static org.coodex.concrete.core.intercept.InterceptOrders.OTHER;
import static org.coodex.concrete.core.intercept.TBTSManager.*;
import static org.coodex.util.GenericTypeHelper.solveFromInstance;

@ServerSide
@TestContext
public abstract class AbstractTokenBasedTopicSubscribeInterceptor<M extends Serializable> extends AbstractInterceptor {


    @Inject
    private Token token;
    private Singleton<TokenBasedTopic<M>> tokenBasedTopicSingleton = new Singleton<>(
            this::buildTopic
    );

    private TokenBasedTopic<M> buildTopic() {
        MessageConsumer messageConsumer = getClass().getAnnotation(MessageConsumer.class);
        String queue = null;
        Class<? extends AbstractTopic> clz = TokenBasedTopic.class;
        if (messageConsumer != null) {
            queue = messageConsumer.queue();
            if (TokenBasedTopic.class.isAssignableFrom(messageConsumer.topicType())) {
                clz = messageConsumer.topicType();
            }
        }
        if (clz.getTypeParameters().length == 1) {
            return Topics.get(GenericTypeHelper.buildParameterizedType(clz,
                    getMessageType()), queue);
        } else if (clz.getTypeParameters().length == 0) {
            return Topics.get(clz, queue);
        } else {
            throw new ConcreteException(ErrorCodes.UNKNOWN_CLASS, clz);
        }
    }

    private Type getMessageType() {
        return solveFromInstance(AbstractTokenBasedTopicSubscribeInterceptor.class.getTypeParameters()[0],
                this);
    }

    @Override
    protected boolean accept_(DefinitionContext context) {
        return check_();
    }

    protected abstract MessageFilter<M> subscribe();

    private boolean checkAccountCredible() {
        return true;
    }

    protected abstract boolean check();

    private TokenBasedTopic<M> getTopic() {
        return tokenBasedTopicSingleton.getInstance();
    }

    private boolean check_() {

        if (checkAccountCredible()) {
            return token.isAccountCredible() && check();
        } else {
            return check();
        }
    }

    @Override
    public final Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
        if (token.isValid()) {
            if (!Common.isBlank(token.getTokenId())) {
                synchronized (tokenLock(token)) {
                    if (check_()) {
                        MessageFilter<M> observer = subscribe();
                        if (observer != null)
                            putSubscription(getTopic().subscribe(observer));
                    }
                }
            }
        } else {
            cancel(token);
        }
        return super.after(context, joinPoint, result);
    }

    @Override
    public int getOrder() {
        return OTHER + 1;
    }
}
