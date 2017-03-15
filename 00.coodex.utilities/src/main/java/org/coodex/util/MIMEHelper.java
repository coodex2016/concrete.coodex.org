package org.coodex.util;

/**
 * 在classpath中添加mimeTypes.properties，逐行配置mimeType.??? = mimeType
 * Created by davidoff shen on 2016-10-19.
 */
public class MIMEHelper {
    private static final Profile mimeTypes = Profile.getProfile("mimeTypes.properties");

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
