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

package org.coodex.concrete.attachments.server;


import org.coodex.concrete.attachments.AttachmentServiceHelper;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

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

        downloadThread.setPriority(AttachmentServiceHelper.ATTACHMENT_PROFILE.getInt("download.priority", 1));
        downloadThread.start();

    }
}
