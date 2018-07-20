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

package org.csource.fastdfs;

import java.util.Date;

import static org.coodex.util.Common.dateToStr;

/**
 * Server Info
 *
 * @author Happy Fish / YuQing
 * @version Version 1.23
 */
public class FileInfo {
    protected String source_ip_addr;
    protected long file_size;
    protected Date create_timestamp;
    protected int crc32;

    /**
     * Constructor
     *
     * @param file_size        the file size
     * @param create_timestamp create timestamp in seconds
     * @param crc32            the crc32 signature
     * @param source_ip_addr   the source storage ip address
     */
    public FileInfo(long file_size, int create_timestamp, int crc32, String source_ip_addr) {
        this.file_size = file_size;
        this.create_timestamp = new Date(create_timestamp * 1000L);
        this.crc32 = crc32;
        this.source_ip_addr = source_ip_addr;
    }

    /**
     * get the source ip address of the file uploaded to
     *
     * @return the source ip address of the file uploaded to
     */
    public String getSourceIpAddr() {
        return this.source_ip_addr;
    }

    /**
     * set the source ip address of the file uploaded to
     *
     * @param source_ip_addr the source ip address
     */
    public void setSourceIpAddr(String source_ip_addr) {
        this.source_ip_addr = source_ip_addr;
    }

    /**
     * get the file size
     *
     * @return the file size
     */
    public long getFileSize() {
        return this.file_size;
    }

    /**
     * set the file size
     *
     * @param file_size the file size
     */
    public void setFileSize(long file_size) {
        this.file_size = file_size;
    }

    /**
     * get the create timestamp of the file
     *
     * @return the create timestamp of the file
     */
    public Date getCreateTimestamp() {
        return this.create_timestamp;
    }

    /**
     * set the create timestamp of the file
     *
     * @param create_timestamp create timestamp in seconds
     */
    public void setCreateTimestamp(int create_timestamp) {
        this.create_timestamp = new Date(create_timestamp * 1000L);
    }

    /**
     * get the file CRC32 signature
     *
     * @return the file CRC32 signature
     */
    public long getCrc32() {
        return this.crc32;
    }

    /**
     * set the create timestamp of the file
     *
     * @param crc32 the crc32 signature
     */
    public void setCrc32(int crc32) {
        this.crc32 = crc32;
    }

    /**
     * to string
     *
     * @return string
     */
    public String toString() {
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "source_ip_addr = " + this.source_ip_addr + ", " +
                "file_size = " + this.file_size + ", " +
                "create_timestamp = " + dateToStr(this.create_timestamp) + ", " +
                "crc32 = " + this.crc32;
    }
}
