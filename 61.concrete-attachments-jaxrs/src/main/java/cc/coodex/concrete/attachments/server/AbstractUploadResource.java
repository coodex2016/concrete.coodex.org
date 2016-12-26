package cc.coodex.concrete.attachments.server;

import cc.coodex.concrete.attachments.AttachmentEntityInfo;
import cc.coodex.concrete.attachments.AttachmentInfo;
import cc.coodex.concrete.attachments.Repository;
import cc.coodex.concrete.attachments.client.ClientService;
import cc.coodex.concrete.common.Assert;
import cc.coodex.concrete.common.BeanProviderFacade;
import cc.coodex.concrete.jaxrs.Client;

import java.io.InputStream;

import static cc.coodex.concrete.attachments.AttachmentServiceHelper.ATTACHMENT_PROFILE;
import static cc.coodex.concrete.common.AttachmentInfoErrorCodes.NO_READ_PRIVILEGE;
import static cc.coodex.concrete.common.AttachmentInfoErrorCodes.NO_WRITE_PRIVILEGE;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AbstractUploadResource {

    private Repository repository = BeanProviderFacade.getBeanProvider().getBean(Repository.class);

    protected final AttachmentEntityInfo saveToRepo(String clientId, String tokenId, AttachmentInfo attachmentInfo, InputStream inputStream) {

        Assert.is(ATTACHMENT_PROFILE.getBool(clientId + ".readonly", true), NO_WRITE_PRIVILEGE);

        ClientService clientService = Client.getBean(ClientService.class,
                ATTACHMENT_PROFILE.getString(clientId + ".location"));
        Assert.not(clientService.writable(tokenId), NO_WRITE_PRIVILEGE);
        attachmentInfo.setLastUsed(System.currentTimeMillis());
        AttachmentEntityInfo entityInfo = repository.put(inputStream, attachmentInfo);

        if (!"public".equalsIgnoreCase(ATTACHMENT_PROFILE.getString("rule.read", "public"))) {
            clientService.notify(tokenId, entityInfo.getId());
        }
        return entityInfo;
    }

}
