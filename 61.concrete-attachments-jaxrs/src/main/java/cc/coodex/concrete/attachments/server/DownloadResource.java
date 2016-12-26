package cc.coodex.concrete.attachments.server;


import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import static cc.coodex.concrete.attachments.AttachmentServiceHelper.ATTACHMENT_PROFILE;

/**
 * 附件下载的一个参考实现，基于jaxrs2.0
 *
 * /attachments/download/{attachmentId};c=clientId;t=tokenId
 * Created by davidoff shen on 2016-12-13.
 */
@Path("attachments/download")
public class DownloadResource extends AbstractDownloadResource {

    @Path("/{attachmentId}")
    @GET
    public void download(@Suspended final AsyncResponse asyncResponse,
                         @MatrixParam("c") final String clientId,
                         @MatrixParam("t") final String tokenId,
                         @PathParam("attachmentId") final String attachmentId) {

        Thread downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    asyncResponse.resume(download(clientId, tokenId, attachmentId));
                } catch (Throwable th) {
                    asyncResponse.resume(th);
                }
            }
        });

        downloadThread.setPriority(ATTACHMENT_PROFILE.getInt("download.priority", 1));
        downloadThread.start();

    }
}
