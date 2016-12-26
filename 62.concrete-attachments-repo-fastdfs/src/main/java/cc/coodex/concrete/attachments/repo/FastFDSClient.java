/*
 * Copyright (c) 2016 coodex.org
 *    ___                _
 *   / __\___   ___   __| | _____  __  ___  _ __ __ _
 *  / /  / _ \ / _ \ / _` |/ _ \ \/ / / _ \| '__/ _` |
 * / /__| (_) | (_) | (_| |  __/>  < | (_) | | | (_| |
 * \____/\___/ \___/ \__,_|\___/_/\_(_)___/|_|  \__, |
 *                                              |___/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.coodex.concrete.attachments.repo;

import cc.coodex.util.Common;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by davidoff shen on 2016-10-13.
 */
public class FastFDSClient {

    static {
        ClientGlobal.setG_charset("UTF-8");
    }

    public static final String META_FILE_NAME = "file-name";

    class StorageClient_ extends StorageClient {
        StorageClient_(TrackerServer trackerServer, StorageServer storageServer) {
            super(trackerServer, null);
        }

        public String[] do_upload_file(byte cmd, String group_name, String master_filename, String prefix_name, String file_ext_name, long file_size, UploadCallback callback, NameValuePair[] meta_list) throws IOException, MyException {
            return super.do_upload_file(cmd, group_name, master_filename, prefix_name, file_ext_name, file_size, callback, meta_list);
        }

        public int do_append_file(String group_name, String appender_filename, long file_size, UploadCallback callback) throws IOException, MyException {
            return super.do_append_file(group_name, appender_filename, file_size, callback);
        }

        public int do_modify_file(String group_name, String appender_filename, long file_offset, long modify_size, UploadCallback callback) throws IOException, MyException {
            return super.do_modify_file(group_name, appender_filename, file_offset, modify_size, callback);
        }
    }

    static Map<String, String> nameValuePairToMap(NameValuePair[] nameValuePairs) {
        Map<String, String> map = new HashMap<String, String>();
        if (nameValuePairs != null && nameValuePairs.length > 0) {
            for (NameValuePair pair : nameValuePairs) {
                map.put(pair.getName(), pair.getValue());
            }
        }
        return map;
    }

    static NameValuePair[] mapToNameValuePairs(Map<String, String> map) {
        if (map == null) return null;
        Set<NameValuePair> set = new HashSet<NameValuePair>();

        for (String key : map.keySet()) {
            set.add(new NameValuePair(key, map.get(key)));
        }
        return set.toArray(new NameValuePair[0]);
    }


    private TrackerGroup trackerGroup;

    public FastFDSClient(TrackerGroup trackerGroup) {
        this.trackerGroup = trackerGroup;
    }


    private StorageClient_ getClient(String groupName) throws IOException {

        TrackerClient trackerClient = new TrackerClient(trackerGroup);
        TrackerServer trackerServer = trackerGroup == null ? null : trackerGroup.getConnection();
        return new StorageClient_(trackerServer, trackerClient.getStoreStorage(trackerServer, groupName));
    }


    /**
     * 上传一个本地文件
     *
     * @param filePath
     * @return
     * @throws IOException
     * @throws MyException
     */
    public FileLocation uploadFile(String filePath) throws IOException, MyException {
        return uploadFile(filePath, null);
    }

    public FileLocation uploadFile(String filePath, String group) throws IOException, MyException {
        File f = new File(filePath);
        if (!f.exists()) throw new FileNotFoundException(filePath);
        InputStream inputStream = new FileInputStream(f);
        try {
            return uploadFile(inputStream, f.getName(), f.length(), group);
        } finally {
            inputStream.close();
        }
    }

    public FileLocation uploadFile(InputStream inputStream,
                                   String local_filename, String group) throws IOException, MyException {
        return uploadFile(inputStream, local_filename, 0, group);
    }

    /**
     * @param inputStream
     * @param local_filename 文件名
     * @param size           文件大小
     * @return
     * @throws IOException
     * @throws MyException
     */
    public FileLocation uploadFile(InputStream inputStream,
                                   String local_filename,
                                   long size, String group) throws IOException, MyException {
        return uploadFile(inputStream, local_filename, null, size, null, group);
    }

    /**
     * @param inputStream
     * @param local_filename
     * @param size
     * @param metaInfo
     * @return
     * @throws IOException
     * @throws MyException
     */
    public FileLocation uploadFile(InputStream inputStream,
                                   String local_filename,
                                   long size,
                                   Map<String, String> metaInfo) throws IOException, MyException {
        return uploadFile(inputStream, local_filename, size, metaInfo, null);
    }

    public FileLocation uploadFile(InputStream inputStream,
                                   String local_filename,
                                   long size,
                                   Map<String, String> metaInfo, String group) throws IOException, MyException {
        return uploadFile(inputStream, local_filename, null, size, metaInfo, group);
    }


    /**
     * 上传一个流
     *
     * @param inputStream
     * @param local_filename
     * @param file_ext_name
     * @param size
     * @param meta_list
     * @return
     * @throws IOException
     * @throws MyException
     */
    public FileLocation uploadFile(
            InputStream inputStream,
            String local_filename,
            String file_ext_name,
            long size,
            Map<String, String> meta_list, String group) throws IOException, MyException {

        meta_list = meta_list == null ? new HashMap<String, String>() : meta_list;

        if (file_ext_name == null) {
            if (Common.isBlank(local_filename)) {
                file_ext_name = "";
            } else {
                int nPos = local_filename.lastIndexOf('.');
                if (nPos > 0 && local_filename.length() - nPos <= ProtoCommon.FDFS_FILE_EXT_NAME_MAX_LEN + 1) {
                    file_ext_name = local_filename.substring(nPos + 1);
                }
            }
        }

        if (meta_list != null && meta_list.get(META_FILE_NAME) == null && !Common.isBlank(local_filename)) {
            meta_list.put(META_FILE_NAME, local_filename);
        }

        String[] result = getClient(group).do_upload_file(ProtoCommon.STORAGE_PROTO_CMD_UPLOAD_FILE,
                null, local_filename, null, file_ext_name, size <= 0 ? inputStream.available() : size,
                new UploadStream(inputStream, size), mapToNameValuePairs(meta_list));

        return new FileLocation(result[0], result[1]);
    }


    public FileSummary getFileSummary(String group, String fileName) throws IOException, MyException {
        StorageClient_ client = getClient(group);
        FileSummary fileSummary = new FileSummary(client.get_file_info(group, fileName));
        fileSummary.setMetaData(nameValuePairToMap(client.get_metadata(group, fileName)));
        return fileSummary;
    }

    public FileSummary getFileSummary(FileLocation location) throws IOException, MyException {
        return getFileSummary(location.getGroup(), location.getPath());
    }

    public void downloadFile(FileLocation location, OutputStream outputStream) throws IOException, MyException {
        downloadFile(location.getGroup(), location.getPath(), outputStream);
    }

    public void downloadFile(String group, String fileName, final OutputStream outputStream) throws IOException, MyException {
        downloadFile(group, fileName, outputStream, 0, 0);
    }

    public void downloadFile(String group, String fileName, final OutputStream outputStream, long offset, long length) throws IOException, MyException {
        if (outputStream == null) throw new NullPointerException("stream is NULL!");

        getClient(group).download_file(group, fileName, offset, length, new DownloadCallback() {
            public int recv(long file_size, byte[] data, int bytes) {
                try {
                    outputStream.write(data, 0, bytes);
                    outputStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException("write error: " + e.getLocalizedMessage(), e);
                }
                return 0;
            }
        });
    }

    public void deleteFile(FileLocation location) throws IOException, MyException {
        deleteFile(location.getGroup(), location.getPath());
    }

    public void deleteFile(String group, String fileName) throws IOException, MyException {
        int i = getClient(group).delete_file(group, fileName);
        if (i != 0) throw new MyException("delete error. errorCode: " + i);

    }


    public void updateFileSummary(FileLocation location, FileSummary fileSummary) throws IOException, MyException {
        getClient(location.getGroup()).set_metadata(
                location.getGroup(),
                location.getPath(),
                mapToNameValuePairs(fileSummary.getMetaData()),
                ProtoCommon.STORAGE_SET_METADATA_FLAG_OVERWRITE
        );
    }


}
