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

package org.coodex.concrete.attachments;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * 附件仓库，只关注附件存取
 * Created by davidoff shen on 2016-12-13.
 */
public interface Repository {

    AttachmentEntityInfo put(InputStream content, AttachmentInfo metaInfo);

    void updateInfo(String attachmentId, AttachmentInfo metaInfo);

    void delete(String attachmentId);

    void delete(Set<String> attachmentIds);

    AttachmentEntityInfo get(String attachmentId);

    void writeTo(String attachmentId, OutputStream outputStream, int speedLimit);

}
