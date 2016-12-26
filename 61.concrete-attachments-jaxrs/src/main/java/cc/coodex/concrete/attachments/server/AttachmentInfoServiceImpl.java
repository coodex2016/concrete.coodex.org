package cc.coodex.concrete.attachments.server;

import cc.coodex.concrete.attachments.AttachmentEntityInfo;
import cc.coodex.concrete.attachments.Repository;
import cc.coodex.concrete.common.Assert;
import cc.coodex.concrete.common.AttachmentInfoErrorCodes;
import cc.coodex.concrete.common.BeanProviderFacade;

import java.util.ArrayList;
import java.util.List;

import static cc.coodex.concrete.attachments.AttachmentServiceHelper.verify;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AttachmentInfoServiceImpl implements AttachmentInfoService {

    private Repository attachmentRepository = BeanProviderFacade.getBeanProvider().getBean(Repository.class);


    @Override
    public AttachmentEntityInfo get(String attachmentId, String clientId, String sign) {
        Assert.not(verify(clientId, attachmentId, sign), AttachmentInfoErrorCodes.VERIFY_FAILED);
        return attachmentRepository.get(attachmentId);
    }

    @Override
    public List<AttachmentEntityInfo> list(List<String> attachmentIds, String clientId, String sign) {
        Assert.not(verify(clientId, attachmentIds, sign), AttachmentInfoErrorCodes.VERIFY_FAILED);
        List<AttachmentEntityInfo> list = new ArrayList<AttachmentEntityInfo>();
        for (String attachmentId : attachmentIds) {
            list.add(attachmentRepository.get(attachmentId));
        }
        return list;
    }
}
