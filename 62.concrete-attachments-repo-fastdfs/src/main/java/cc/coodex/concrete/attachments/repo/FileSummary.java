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

import org.csource.fastdfs.FileInfo;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-10-13.
 */
public class FileSummary extends FileInfo {
    private Map<String, String> metaData;

    public FileSummary(FileInfo fileInfo) {
        super(fileInfo.getFileSize(),
                (int) (fileInfo.getCreateTimestamp().getTime() / 1000),
                (int) fileInfo.getCrc32(),
                fileInfo.getSourceIpAddr());
    }

    void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public String getMetaData(String name) {
        return metaData == null ? null : metaData.get(name);
    }


}
