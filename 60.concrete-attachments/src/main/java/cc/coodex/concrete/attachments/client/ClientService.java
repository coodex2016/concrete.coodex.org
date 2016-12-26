package cc.coodex.concrete.attachments.client;

import cc.coodex.concrete.api.MicroService;
import cc.coodex.concrete.attachments.AbstractAttachmentService;

/**
 * Created by davidoff shen on 2016-12-13.
 */
@MicroService("client")
public interface ClientService extends AbstractAttachmentService {

    boolean readable(String token, String attachmentId);

    boolean writable(String token);

    boolean deletable(String token, String attachmentId);

    void notify(String token, String attachmentId);

}
