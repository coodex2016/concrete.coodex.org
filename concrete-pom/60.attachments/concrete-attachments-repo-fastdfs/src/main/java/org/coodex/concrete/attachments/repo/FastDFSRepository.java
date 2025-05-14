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

package org.coodex.concrete.attachments.repo;

import org.coodex.concrete.attachments.AttachmentEntityInfo;
import org.coodex.concrete.attachments.AttachmentInfo;
import org.coodex.concrete.attachments.Repository;
import org.coodex.config.Config;
import org.coodex.io.SpeedLimitedOutputStream;
import org.coodex.util.Base58;
import org.coodex.util.Clock;
import org.csource.fastdfs.TrackerGroup;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

//import org.coodex.util.Profile_Deprecated;

/**
 * 基于fastDFS的参考存储
 * <p>
 * 因fastdfs-client未发布到maven中央库，本包中集成了其代码，请使用者遵守原代码的协议
 * Created by davidoff shen on 2016-12-14.
 */
public class FastDFSRepository implements Repository {

    private static final String TAG_ATTACHMENT_SERVICE = "attachmentService";

//    public static final Profile_Deprecated ATTACHMENT_PROFILE = Profile_Deprecated.getProfile("attachmentService.properties");

    private static InetSocketAddress[] getTrackerAddress() {
        String[] address = Config.getArray("fastdfs.tracker", TAG_ATTACHMENT_SERVICE, getAppSet());
        if (address == null)
            return null;
        Set<InetSocketAddress> addressList = new HashSet<InetSocketAddress>();
        for (String addr : address) {
            int index = addr.indexOf(':');
            if (index > 0) {
                addressList.add(new InetSocketAddress(addr.substring(0, index).trim(),
                        Integer.valueOf(addr.substring(index + 1).trim())));
            }
        }
        return addressList.toArray(new InetSocketAddress[0]);
    }

    private FastFDSClient getClient() {
        return new FastFDSClient(new TrackerGroup(getTrackerAddress()));
    }

    private Map<String, String> toMap(AttachmentInfo attachmentInfo) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", attachmentInfo.getName());
        map.put("contentType", attachmentInfo.getContentType());
        map.put("size", String.valueOf(attachmentInfo.getSize()));
        map.put("created", String.valueOf(attachmentInfo.getCreated()));
        map.put("lastUsed", String.valueOf(attachmentInfo.getLastUsed()));
        map.put("owner", String.valueOf(attachmentInfo.getOwner()));
        return map;
    }

    private void copyToInfo(AttachmentInfo attachmentInfo, Map<String, String> meta) {
        attachmentInfo.setName(meta.get("name"));
        attachmentInfo.setContentType(meta.get("contentType"));
        attachmentInfo.setSize(Long.valueOf(meta.get("size")));
        attachmentInfo.setCreated(Long.valueOf(meta.get("created")));
        attachmentInfo.setLastUsed(Long.valueOf(meta.get("lastUsed")));
        attachmentInfo.setOwner(meta.get("owner"));
    }

    private AttachmentEntityInfo copy(AttachmentInfo metaInfo) {
        AttachmentEntityInfo entityInfo = new AttachmentEntityInfo();
        entityInfo.setContentType(metaInfo.getContentType());
        entityInfo.setCreated(metaInfo.getCreated());
        entityInfo.setSize(metaInfo.getSize());
        entityInfo.setOwner(metaInfo.getOwner());
        entityInfo.setLastUsed(metaInfo.getLastUsed());
        entityInfo.setName(metaInfo.getName());
        return entityInfo;
    }

    private String toId(FileLocation location) {
        try {
            return Base58.encode((location.getGroup() + '/' + location.getPath()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private FileLocation idToFileLocation(String id) {
        try {
            byte[] buf = Base58.decode(id);
            String s = new String(buf, "UTF-8");
            int sp = s.indexOf('/');
            return new FileLocation(s.substring(0, sp), s.substring(sp + 1));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AttachmentEntityInfo put(InputStream content, AttachmentInfo metaInfo) {
        metaInfo.setCreated(Clock.currentTimeMillis());
        FastFDSClient client = getClient();
        try {
            FileLocation location = client.uploadFile(content, metaInfo.getName(), metaInfo.getSize(), toMap(metaInfo));
            AttachmentEntityInfo entityInfo = copy(metaInfo);
            entityInfo.setId(toId(location));
            return entityInfo;
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }

    }

    protected void update(AttachmentEntityInfo entityInfo) {
        entityInfo.setLastUsed(Clock.currentTimeMillis());
        updateInfo(entityInfo.getId(), entityInfo);
    }


    @Override
    public void updateInfo(String attachmentId, AttachmentInfo metaInfo) {
        try {
            FastFDSClient client = getClient();
            FileLocation fileLocation = idToFileLocation(attachmentId);
            FileSummary summary = client.getFileSummary(fileLocation);
            summary.setMetaData(toMap(metaInfo));
            client.updateFileSummary(fileLocation, summary);
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    @Override
    public void delete(String attachmentId) {
        try {
            FileLocation fileLocation = idToFileLocation(attachmentId);
            getClient().deleteFile(fileLocation);
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    @Override
    public void delete(Set<String> attachmentIds) {
        for (String id : attachmentIds) {
            delete(id);
        }
    }

    @Override
    public AttachmentEntityInfo get(String attachmentId) {
        try {
            FileSummary summary = getClient().getFileSummary(idToFileLocation(attachmentId));
            AttachmentEntityInfo entityInfo = new AttachmentEntityInfo();
            copyToInfo(entityInfo, summary.getMetaData());
            entityInfo.setId(attachmentId);
            return entityInfo;
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    @Override
    public void writeTo(String attachmentId, OutputStream outputStream, int speedLimit) {
        try {
            FastFDSClient client = getClient();
            client.downloadFile(
                    idToFileLocation(attachmentId),
                    new SpeedLimitedOutputStream(outputStream, speedLimit));
            AttachmentEntityInfo entityInfo = get(attachmentId);
            entityInfo.setLastUsed(Clock.currentTimeMillis());
            update(entityInfo);
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }
}
