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

package org.coodex.concrete.attachments;

import org.coodex.concrete.common.AttachmentInfoErrorCodes;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.config.Config;
import org.coodex.util.DigestHelper;

import java.util.List;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AttachmentServiceHelper {
    public static final String TAG_ATTACHMENT_SERVICE = "attachmentService";

//    public static final Profile_Deprecated ATTACHMENT_PROFILE = Profile_Deprecated.getProfile("attachmentService.properties");


    public static String getKey(String clientId) {
        return Config.get("key." + clientId, "", TAG_ATTACHMENT_SERVICE, getAppSet());
    }

    public static String sign(String clientId, String attachmentId) {
        try {
            return DigestHelper.hmac(attachmentId, getKey(clientId));
        } catch (Throwable t) {
            throw new ConcreteException(AttachmentInfoErrorCodes.HMAC_ERROR, t);
        }
    }

    public static String sign(String clientId, List<String> attachmentIds) {
        StringBuilder builder = new StringBuilder();
        for (String attachmentId : attachmentIds)
            builder.append(attachmentId);

        try {
            return DigestHelper.hmac(builder.toString(), getKey(clientId));
        } catch (Throwable t) {
            throw new ConcreteException(AttachmentInfoErrorCodes.HMAC_ERROR, t);
        }
    }

    public static boolean verify(String clientId, String attachmentId, String sign) {
        return sign(clientId, attachmentId).equalsIgnoreCase(sign);
    }

    public static boolean verify(String clientId, List<String> attachmentIds, String sign) {
        return sign(clientId, attachmentIds).equalsIgnoreCase(sign);
    }
}
