package org.coodex.concrete.attachments;

import org.coodex.concrete.common.AttachmentInfoErrorCodes;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.util.DigestHelper;
import org.coodex.util.Profile;

import java.util.List;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AttachmentServiceHelper {

    public static final Profile ATTACHMENT_PROFILE = Profile.getProfile("attachmentService.properties");


    public static String getKey(String clientId) {
        return ATTACHMENT_PROFILE.getString("key." + clientId, "");
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
