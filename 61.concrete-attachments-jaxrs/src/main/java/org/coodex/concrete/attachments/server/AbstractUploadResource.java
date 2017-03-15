package org.coodex.concrete.attachments.server;

import org.coodex.concrete.attachments.AttachmentEntityInfo;
import org.coodex.concrete.attachments.AttachmentInfo;
import org.coodex.concrete.attachments.AttachmentServiceHelper;
import org.coodex.concrete.attachments.Repository;
import org.coodex.concrete.attachments.client.ClientService;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.AttachmentInfoErrorCodes;
import org.coodex.concrete.common.BeanProviderFacade;
import org.coodex.concrete.jaxrs.Client;

import java.io.InputStream;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AbstractUploadResource {

    private Repository repository = BeanProviderFacade.getBeanProvider().getBean(Repository.class);

    protected final AttachmentEntityInfo saveToRepo(String clientId, String tokenId, AttachmentInfo attachmentInfo, InputStream inputStream) {

        Assert.is(AttachmentServiceHelper.ATTACHMENT_PROFILE.getBool(clientId + ".readonly", true), AttachmentInfoErrorCodes.NO_WRITE_PRIVILEGE);

        ClientService clientService = Client.getBean(ClientService.class,
                AttachmentServiceHelper.ATTACHMENT_PROFILE.getString(clientId + ".location"));
        Assert.not(clientService.writable(tokenId), AttachmentInfoErrorCodes.NO_WRITE_PRIVILEGE);
        attachmentInfo.setLastUsed(System.currentTimeMillis());
        AttachmentEntityInfo entityInfo = repository.put(inputStream, attachmentInfo);

        if (!"public".equalsIgnoreCase(AttachmentServiceHelper.ATTACHMENT_PROFILE.getString("rule.read", "public"))) {
            clientService.notify(tokenId, entityInfo.getId());
        }
        return entityInfo;
    }

}
