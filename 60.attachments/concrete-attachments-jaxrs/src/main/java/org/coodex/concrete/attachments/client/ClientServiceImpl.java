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

package org.coodex.concrete.attachments.client;

import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.Token;
import org.coodex.concrete.core.token.TokenWrapper;
import org.coodex.config.Config;
import org.coodex.util.Clock;
import org.coodex.util.Common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.coodex.concrete.attachments.AttachmentServiceHelper.TAG_ATTACHMENT_SERVICE;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.core.token.TokenWrapper.getToken;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class ClientServiceImpl implements ClientService {


//    private TokenManager tokenManager;
    //= BeanProviderFacade.getBeanProvider().getBean(TokenManager.class);

    public static final String ATTACHMENT_AUTHORIZATION_KEY = ClientServiceImpl.class.getName() + ".AUTHORIZATIONS";
    public static final String ATTACHMENT_WRITABLE_KEY = ClientServiceImpl.class.getName() + ".WRITABLE";
    private static final Token token = TokenWrapper.getInstance();

    public static void allowWrite() {
        if (token.getAttribute(ATTACHMENT_WRITABLE_KEY, Object.class) == null)
            token.setAttribute(ATTACHMENT_WRITABLE_KEY, "OK");
    }


    public static void allow(Set<String> attachmentIds) {
        allow(token, attachmentIds);
    }

    @SuppressWarnings("unchecked")
    private static void allow(Token token, Set<String> attachmentIds) {
        HashMap<String, Long> attachments;
        synchronized (ClientServiceImpl.class) {
            attachments = token.getAttribute(ATTACHMENT_AUTHORIZATION_KEY, HashMap.class);
            if (attachments == null) {
                attachments = new HashMap<String, Long>();
                token.setAttribute(ATTACHMENT_AUTHORIZATION_KEY, attachments);
            }
        }

        long validity = Clock.currentTimeMillis() +
                Config.getValue("attachment.validity", 10, TAG_ATTACHMENT_SERVICE, getAppSet()) * 1000 * 60;
        for (String attachmentId : attachmentIds) {
            if (attachmentId != null) {
                attachments.put(attachmentId, validity);
            }
        }
        token.flush();
    }

    public static void allow(String... attachmentIds) {
        allow(Common.arrayToSet(attachmentIds));
    }


    private Token getTokenById(String tokenId) {
        Token token = getToken(tokenId);
        IF.isNull(token, ErrorCodes.NONE_TOKEN);
        IF.not(token.isValid(), ErrorCodes.TOKEN_INVALIDATE, tokenId);
        return token;
    }

    @SuppressWarnings("unchecked")
    private boolean isAuthorized(String tokenId, String attachmentId) {
        Token token = getTokenById(tokenId);
        HashMap<String, Long> authorizations =
                token.getAttribute(ATTACHMENT_AUTHORIZATION_KEY, HashMap.class);
        Long validity = authorizations.get(attachmentId);
        if (validity == null || validity.longValue() < Clock.currentTimeMillis()) {
            authorizations.remove(attachmentId);
            token.flush();
            return false;
        } else
            return true;
    }


    @Override
    public boolean readable(String token, String attachmentId) {
        return isAuthorized(token, attachmentId);
    }

    @Override
    public boolean writable(String token) {
        return getTokenById(token)
                .getAttribute(ATTACHMENT_WRITABLE_KEY, Object.class) != null;
    }


    @Override
    public boolean deletable(String token, String attachmentId) {
        return writable(token) && readable(token, attachmentId);
    }

    @Override
    public void notify(String token, String attachmentId) {
        Token t = getToken(token);
        if (t != null) {
            Set<String> set = new HashSet<String>();
            set.add(attachmentId);
            allow(t, set);
        }
    }

//    public TokenManager getTokenManager() {
//        if(tokenManager == null)
//            tokenManager = BeanProviderFacade.getBeanProvider().getBean(TokenManager.class);
//        return tokenManager;
//    }
}
