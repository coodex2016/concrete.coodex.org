package cc.coodex.concrete.attachments.server;

import cc.coodex.concrete.api.MicroService;
import cc.coodex.concrete.attachments.AbstractAttachmentService;
import cc.coodex.concrete.attachments.AttachmentEntityInfo;

import java.util.List;

/**
 * Created by davidoff shen on 2016-12-13.
 */
@MicroService("AttachmentEntityInfo")
public interface AttachmentInfoService extends AbstractAttachmentService {

    AttachmentEntityInfo get(String attachmentId, String clientId, String sign);

    List<AttachmentEntityInfo> list(List<String> attachmentIds, String clientId, String sign);
}
