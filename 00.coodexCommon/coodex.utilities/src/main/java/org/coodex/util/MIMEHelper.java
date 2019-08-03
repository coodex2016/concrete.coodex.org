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

package org.coodex.util;

/**
 * 在classpath中添加mimeTypes.properties，逐行配置mimeType.??? = mimeType
 * Created by davidoff shen on 2016-10-19.
 */
public class MIMEHelper {

    private static final Profile mimeTypes = Profile.get("mimeTypes.properties");

    private static final String DEFAULT_TYPE = "application/octet-stream";

    public static String getMimeTypeByFileName(String fileName) {
        if (fileName == null) return DEFAULT_TYPE;
        int indexOfDot = fileName.lastIndexOf('.');
        if (indexOfDot >= 0)
            return getMimeTypeByExtName(fileName.substring(indexOfDot + 1));
        else
            return DEFAULT_TYPE;
    }

    public static String getMimeTypeByExtName(String extName) {
        if (extName == null) return DEFAULT_TYPE;
        String mimeType = mimeTypes.getString("mimeType." + extName.toLowerCase());
        return Common.isBlank(mimeType) ? DEFAULT_TYPE : mimeType;
    }

}
