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
import org.coodex.concrete.attachments.AttachmentInfo;
import org.coodex.concrete.attachments.Repository;
import org.coodex.concrete.attachments.client.ClientService;
import org.coodex.concrete.common.AttachmentInfoErrorCodes;
import org.coodex.concrete.common.BeanProviderFacade;
import org.coodex.concrete.common.IF;
import org.coodex.config.Config;
import org.coodex.util.Clock;

import java.io.InputStream;

import static org.coodex.concrete.attachments.AttachmentServiceHelper.TAG_ATTACHMENT_SERVICE;
import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AbstractUploadResource {

    private Repository repository = BeanProviderFacade.getBeanProvider().getBean(Repository.class);

    protected final AttachmentEntityInfo saveToRepo(String clientId, String tokenId, AttachmentInfo attachmentInfo, InputStream inputStream) {

        IF.is(Config.getValue(clientId + ".readonly", true, TAG_ATTACHMENT_SERVICE, getAppSet()), AttachmentInfoErrorCodes.NO_WRITE_PRIVILEGE);

        ClientService clientService = Client.getInstance(ClientService.class,
                Config.get(clientId + ".location", TAG_ATTACHMENT_SERVICE, getAppSet()));// TODO rename
        IF.not(clientService.writable(tokenId), AttachmentInfoErrorCodes.NO_WRITE_PRIVILEGE);
        attachmentInfo.setLastUsed(Clock.currentTimeMillis());
        AttachmentEntityInfo entityInfo = repository.put(inputStream, attachmentInfo);

        if (!"public".equalsIgnoreCase(Config.getValue("rule.read", "public", TAG_ATTACHMENT_SERVICE, getAppSet()))) {
            clientService.notify(tokenId, entityInfo.getId());
        }
        return entityInfo;
    }

}
