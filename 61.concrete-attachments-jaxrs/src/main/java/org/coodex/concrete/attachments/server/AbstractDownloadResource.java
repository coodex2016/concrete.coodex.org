/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import org.coodex.concrete.attachments.AttachmentEntityInfo;
import org.coodex.concrete.attachments.AttachmentServiceHelper;
import org.coodex.concrete.attachments.Repository;
import org.coodex.concrete.attachments.client.ClientService;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.AttachmentInfoErrorCodes;
import org.coodex.concrete.common.BeanProviderFacade;
import org.coodex.concrete.jaxrs.Client;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by davidoff shen on 2016-12-14.
 */
public class AbstractDownloadResource {

    private Repository repository = BeanProviderFacade.getBeanProvider().getBean(Repository.class);

    protected Response download(String clientId, String tokenId, final String attachmentId) throws UnsupportedEncodingException {

        AttachmentEntityInfo attachmentEntityInfo = repository.get(attachmentId);
        Assert.isNull(attachmentEntityInfo, AttachmentInfoErrorCodes.ATTACHMENT_NOT_EXISTS);

        if (!"public".equalsIgnoreCase(AttachmentServiceHelper.ATTACHMENT_PROFILE.getString("rule.read", "public"))) {

            ClientService clientService = Client.getBean(ClientService.class,
                    AttachmentServiceHelper.ATTACHMENT_PROFILE.getString(clientId + ".location"));
            Assert.not(clientService.readable(tokenId, attachmentId), AttachmentInfoErrorCodes.NO_READ_PRIVILEGE);
        }


        Response.ResponseBuilder builder = Response.ok()
                .header("Content-Type", attachmentEntityInfo.getContentType());

        builder.header("Content-Disposition",
                getContentDispType(attachmentEntityInfo)
                        // TODO 依广勇2011年的测试结果，各浏览器支持模式不同，需要根据不同浏览器选择不同方案
                        + "; fileName=\""
                        + URLEncoder.encode(attachmentEntityInfo.getName(), "UTF-8")
                        + "\"");

        final int speedLimit = AttachmentServiceHelper.ATTACHMENT_PROFILE.getInt("download.speedLimited", 1024) * 1024;

        StreamingOutput output = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {

                repository.writeTo(attachmentId, output, speedLimit > 0 ? speedLimit : Integer.MAX_VALUE);
            }
        };
        return builder.entity(output).build();
    }

    private String getContentDispType(AttachmentEntityInfo resource) {
        String contentType = resource.getContentType().toLowerCase();
        return contentType.startsWith("text") || contentType.startsWith("image") ? "inline"
                : "attachment";
    }
}
