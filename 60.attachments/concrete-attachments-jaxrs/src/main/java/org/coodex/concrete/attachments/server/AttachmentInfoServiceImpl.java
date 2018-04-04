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

import org.coodex.concrete.attachments.AttachmentEntityInfo;
import org.coodex.concrete.attachments.AttachmentServiceHelper;
import org.coodex.concrete.attachments.Repository;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.AttachmentInfoErrorCodes;
import org.coodex.concrete.common.BeanProviderFacade;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AttachmentInfoServiceImpl implements AttachmentInfoService {

    private Repository attachmentRepository = BeanProviderFacade.getBeanProvider().getBean(Repository.class);


    @Override
    public AttachmentEntityInfo get(String attachmentId, String clientId, String sign) {
        Assert.not(AttachmentServiceHelper.verify(clientId, attachmentId, sign), AttachmentInfoErrorCodes.VERIFY_FAILED);
        return attachmentRepository.get(attachmentId);
    }

    @Override
    public List<AttachmentEntityInfo> list(List<String> attachmentIds, String clientId, String sign) {
        Assert.not(AttachmentServiceHelper.verify(clientId, attachmentIds, sign), AttachmentInfoErrorCodes.VERIFY_FAILED);
        List<AttachmentEntityInfo> list = new ArrayList<AttachmentEntityInfo>();
        for (String attachmentId : attachmentIds) {
            list.add(attachmentRepository.get(attachmentId));
        }
        return list;
    }
}
