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

import org.coodex.concrete.Client;
import org.coodex.concrete.attachments.AttachmentEntityInfo;
import org.coodex.concrete.attachments.Repository;
import org.coodex.concrete.attachments.client.ClientService;
import org.coodex.concrete.common.AttachmentInfoErrorCodes;
import org.coodex.concrete.common.BeanServiceLoaderProvider;
import org.coodex.concrete.common.IF;
import org.coodex.config.Config;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.coodex.concrete.attachments.AttachmentServiceHelper.TAG_ATTACHMENT_SERVICE;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

/**
 * Created by davidoff shen on 2016-12-14.
 */
public class AbstractDownloadResource {

    private Repository repository = BeanServiceLoaderProvider.getBeanProvider().getBean(Repository.class);

    protected Response download(String clientId, String tokenId, final String attachmentId) throws UnsupportedEncodingException {

        AttachmentEntityInfo attachmentEntityInfo = repository.get(attachmentId);
        IF.isNull(attachmentEntityInfo, AttachmentInfoErrorCodes.ATTACHMENT_NOT_EXISTS);

        if (!"public".equalsIgnoreCase(Config.getValue("rule.read", "public", TAG_ATTACHMENT_SERVICE, getAppSet()))) {

            ClientService clientService = Client.getInstance(ClientService.class,
                    Config.get(clientId + ".location", TAG_ATTACHMENT_SERVICE, getAppSet())); // TODO rename
            IF.not(clientService.readable(tokenId, attachmentId), AttachmentInfoErrorCodes.NO_READ_PRIVILEGE);
        }


        Response.ResponseBuilder builder = Response.ok()
                .header("Content-Type", attachmentEntityInfo.getContentType());

        builder.header("Content-Disposition",
                getContentDispType(attachmentEntityInfo)
                        // TODO 依广勇2011年的测试结果，各浏览器支持模式不同，需要根据不同浏览器选择不同方案
                        + "; fileName=\""
                        + URLEncoder.encode(attachmentEntityInfo.getName(), "UTF-8")
                        + "\"");

        final int speedLimit = Config.getValue("download.speedLimited", 1024, TAG_ATTACHMENT_SERVICE, getAppSet()) * 1024;

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
