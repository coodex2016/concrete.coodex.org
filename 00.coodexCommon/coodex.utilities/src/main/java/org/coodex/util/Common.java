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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author davidoff
 */
public class Common {

    private static String pathRoot(String pattern) {
        StringBuilder builder = new StringBuilder();
        StringBuilder node = new StringBuilder();
        for (char ch : pattern.toCharArray()) {
            if (ch == '*') break;
            if (ch == '/') {
                builder.append(node).append(ch);
                node = new StringBuilder();
            } else
                node.append(ch);

        }
        if (node.length() > 0)
            builder.append(node);
        return trim(builder.toString());
    }

//    private static String[] toPackages(String[] patterns) {
//        Set<String> result = new LinkedHashSet<String>();
//        for (int i = 0, l = patterns.length; i < l; i++) {
//            result.add(pathRoot(patterns[i]));
//        }
//        return result.toArray(new String[0]);
//    }

    private static Set<PathPattern> toPathPatterns(String[] paths) {
        Set<PathPattern> pathPatterns = new LinkedHashSet<PathPattern>();
        if (paths != null && paths.length > 0) {
            for (String path : paths)
                pathPatterns.add(new PathPattern(path));
        }
        return pathPatterns;
    }

    private static Collection<String> merge(Collection<PathPattern> pathPatterns) {
        List<String> list = new ArrayList<String>();
        for (PathPattern pathPattern : pathPatterns) {
            list.add(pathPattern.path);
        }
        String[] toMerge = list.toArray(new String[0]);
        list.clear();
        Arrays.sort(toMerge, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.length() - o2.length();
            }
        });
        for (String s : toMerge) {
            boolean exits = false;
            for (String x : list) {
                if (s.startsWith(x)) {
                    exits = true;
                    break;
                }
            }
            if (!exits) {
                list.add(s);
            }
        }
        return list;
    }

    public static void forEach(Processor processor, final ResourceFilter filter, String... paths) {
        try {
            final Set<PathPattern> pathPatterns = toPathPatterns(paths);
            ResourceFilter resourceFilter = new ResourceFilter() {
                @Override
                public boolean accept(String root, String resourceName) {
                    boolean pathOk = false;
                    for (PathPattern pathPattern : pathPatterns) {
                        if (pathPattern.pattern.matcher(resourceName).matches()) {
                            pathOk = true;
                            break;
                        }
                    }
                    return pathOk && filter.accept(root, resourceName);
                }
            };
            for (String path : merge(pathPatterns)) {
                path = trim(path, '/');
                Enumeration<URL> resourceRoots = Common.class.getClassLoader().getResources(path);
                while (resourceRoots.hasMoreElements()) {
                    URL url = resourceRoots.nextElement();
                    String urlStr = url.toString();
                    int indexOfZipMarker = urlStr.indexOf('!');
                    String resourceRoot = urlStr.substring(0, urlStr.length() - path.length() - 1);

                    // 针对每一个匹配的包进行检索
                    if (indexOfZipMarker > 0) {
                        // .zip, .jar
                        File f = new File(new URI(url.getFile()));
                        String fileName = f.getAbsolutePath();

                        forEachInZip(resourceRoot, path, processor, resourceFilter, new File(
                                fileName.substring(0, fileName.length() - 2 - path.length())
                        ));
                    } else {
                        // 文件夹
                        forEachInDir(resourceRoot, path.replace('\\', '/'), processor, resourceFilter, new File(url.toURI()), true);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("resource search failed. {}.", e.getLocalizedMessage(), e);
        } catch (URISyntaxException e) {
            log.warn("resource search failed. {}.", e.getLocalizedMessage(), e);
        }
    }

    //
    private static void forEachInZip(String root, String path, Processor processor, ResourceFilter filter, File zipFile) throws IOException {
        ZipFile zip = new ZipFile(zipFile);

        log.debug("Scan items in [{}]:{{}}", zipFile.getAbsolutePath(), path);
        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String entryName = entry.getName();
                // 此包中的检索
                if (entryName.startsWith(path) && filter.accept(root, entryName)) {
                    processor.process(new URL(root + "/" + entryName), entryName);
                }
            }
        } finally {
            zip.close();
        }
    }

    private static void forEachInDir(String root, String path,
                                     Processor processor, ResourceFilter filter,
                                     File dir, boolean header) throws MalformedURLException {
        if (header)
            log.debug("Scan items in dir[{}]:[{}]", dir.getAbsolutePath(), path);

        //noinspection ConstantConditions
        for (File f : dir.listFiles()) {
            String resourceName = path + '/' + f.getName();
            if (f.isDirectory()) {
                forEachInDir(root, resourceName, processor, filter, f, false);
            } else {
                if (filter.accept(root, resourceName)) {
                    processor.process(new URL(root + '/' + resourceName), resourceName);
                }
            }
        }
    }

    public interface ResourceFilter {
        boolean accept(String root, String resourceName);
    }

    private Common(){}

    private static final String LINE_SEPARATOR = System
            .getProperty("line.separator");
    public static final String PATH_SEPARATOR = System
            .getProperty("path.separator");
    public static final String FILE_SEPARATOR = System
            .getProperty("file.separator");
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String DEFAULT_DATETIME_FORMAT =
            DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT;
    private final static Logger log = LoggerFactory.getLogger(Common.class);
    private static final int TO_LOWER = 'a' - 'A';
    private final static String DEFAULT_DELIM = ".-_ /\\";
    private static ThreadLocal<SingletonMap<String, DateFormat>> threadLocal
            = new ThreadLocal<SingletonMap<String, DateFormat>>();
    private static Singleton<AcceptableServiceLoader<Class<?>, StringConvertWithDefaultValue>> converterServiceLoader
            = new Singleton<AcceptableServiceLoader<Class<?>, StringConvertWithDefaultValue>>(
            new Singleton.Builder<AcceptableServiceLoader<Class<?>, StringConvertWithDefaultValue>>() {
                @Override
                public AcceptableServiceLoader<Class<?>, StringConvertWithDefaultValue> build() {
                    return new AcceptableServiceLoader<Class<?>, StringConvertWithDefaultValue>() {
                    };
                }
            }
    );


    public static <T> Set<T> arrayToSet(T[] array) {
        return new HashSet<T>(Arrays.asList(array));
    }

    public static String getUUIDStr() {
        return UUIDHelper.getUUIDString();
    }

    public static String sha1(String content) {
        byte[] buf = content == null ? new byte[0] : content.getBytes();
        return DigestHelper.sha1(buf);
    }

    public static <T> boolean inArray(T el, T[] array) {
//        boolean in = false;
//        for (T t : array) {
//            if ((el == t) || (t != null && t.equals(el))) {
//                in = true;
//                break;
//            }
//        }
//        return in;
        return findInArray(el, array) >= 0;
    }

    public static <T> int findInArray(T el, T[] array) {
//        boolean in = false;
        for (int i = 0; i < array.length; i++) {
            T t = array[i];
            if ((el == t) || (t != null && t.equals(el))) {
                return i;
//                in = true;
//                break;
            }
        }
        return -1;
    }

    public static String nullToStr(String str) {
        return str == null ? "" : str;
    }

    public static byte[] serialize(Object object) throws IOException {
        // 序列化obj
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(object);
            return bos.toByteArray();
        } finally {
            oos.close();
        }
    }

    public static Object deserialize(byte[] buf) throws IOException, ClassNotFoundException {
        // 反序列化成一个clone对象
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        ObjectInputStream ois = new ObjectInputStream(bis);
        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }

    public static <T extends Serializable> T deepCopy(T object)
            throws IOException, ClassNotFoundException {
        //noinspection unchecked
        return (T) deserialize(serialize(object));
    }

    public static int random(int max) {
        return random(0, max);
    }

    public static int random(int min, int max) {
//        if (max == Integer.MAX_VALUE)
//            max = max - 1;
//        return min + (int) (Math.random() * (max - min + 1));
        return (int) random((long) min, (long) max);
    }

    public static long random(long min, long max) {

        if (min == max) return min;
        float _min = Math.min(min, max);
        float _max = Math.max(min, max);

        return (long) (_min + Math.random() * (_max - _min));
    }

    public static double random(double min, double max) {

        if (min == max) return min;
        double _min = Math.min(min, max);
        double _max = Math.max(min, max);
        return _min + Math.random() * (_max - _min);
    }

    public static <K extends Serializable, V extends Serializable> void copyMap(
            Map<K, V> org, Map<K, V> target) {
        for (K key : org.keySet())
            try {
                target.put(deepCopy(key), deepCopy(org.get(key)));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        copyStream(is, os, 4096, false, Integer.MAX_VALUE);
    }

    public static void copyStream(InputStream is, OutputStream os,
                                  int blockSize, boolean flushPerBlock, int bps) throws IOException {
        byte[] buf = new byte[blockSize];
        int cached;
        long start = Clock.currentTimeMillis();
        long wrote = 0;
        while ((cached = is.read(buf)) > 0) {
            // 自上次计时开始，已写入数据超出bps
            if (wrote >= bps) {
                long n = wrote / bps;
                wrote = wrote % bps;
                long interval = Clock.currentTimeMillis() - start;
                try {
                    if (interval < 1000 * n)
                        Clock.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // 重新开始计时
            start = Clock.currentTimeMillis();
            os.write(buf, 0, cached);
            if (flushPerBlock) {
                os.flush();
                wrote += cached;
            }
        }
        if (!flushPerBlock)
            os.flush();
    }

    public static String byte2hex(byte[] b) {
        return byte2hex(b, 0, b.length);
    }

    public static String byte2hex(byte[] b, int offset, int length) {
        return byte2hex(b, offset, length, 0, null);
    }

    public static String byte2hex(byte[] b, int col, String split) {
        return byte2hex(b, 0, b.length, col, split);
    }

    public static String byte2hex(byte[] b, int offset, int length, int col, String split) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0, n = offset, l = Math.min(offset + length, b.length); n < l; n++, index++) {
            if (col > 0 && index > 0 && index % col == 0) {
                builder.append(LINE_SEPARATOR);
            }
            int tmp = b[n] & 0xFF;
            if (tmp < 0x10) {
                builder.append('0').append(Integer.toHexString(tmp));
            } else {
                builder.append(Integer.toHexString(tmp));
            }
            if (split != null) {
                builder.append(split);
            }
        }
        return builder.toString();
    }

    private static int hexCharValue(char c) {
        if (c >= '0' && c <= '9') return c - '0';

        if (c >= 'a' && c <= 'f') return c - 'a' + 10;

        if (c >= 'A' && c <= 'F') return c - 'A' + 10;

        return -1;
    }


    public static byte[] hex2byte(String hexString) {
        return hex2byte(hexString, LINE_SEPARATOR + " ");
    }

    public static byte[] hex2byte(String hexString, String ignoreChars) {
        char[] ignore = ignoreChars == null ? new char[0] : ignoreChars.toCharArray();
        int hi = -1, low = -1;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean closed = true;
        for (char c : hexString.toCharArray()) {
            int charValue = hexCharValue(c);
            if (charValue == -1) {
                if (inArray(c, ignore)) {
                    closed = true;
                } else {
                    throw new RuntimeException("unknown hex char: " + c + "[0x" + Integer.toHexString(c) + "]");
                }
            } else {
                hi = low;
                low = charValue;
                closed = hi != -1 && low != -1;
            }

            if (closed && low != -1) {
                byteArrayOutputStream.write(hi == -1 ? low : ((hi << 4 | low)));
                hi = -1;
                low = -1;
            }
        }
        if (low != -1) {
            byteArrayOutputStream.write(hi == -1 ? low : ((hi << 4 | low)));
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 交集
     *
     * @param set1
     * @param set2
     * @return
     */
    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<T>(set1);
        result.retainAll(set2);
        return result;
    }

    /**
     * 差集 org - todiv
     *
     * @param org
     * @param todiv
     * @return
     */
    public static <T> Set<T> difference(Set<T> org, Set<T> todiv) {
        Set<T> result = new HashSet<T>(org);
        result.removeAll(todiv);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T, C extends Collection<T>> C join(C instance, Collection... collections) {
        if (collections != null && collections.length > 0) {
            for (Collection c : collections) {
                if (c != null) instance.addAll(c);
            }
        }
        return instance;
    }

    /**
     * 并集
     *
     * @param sets
     * @return
     */
    public static <T> Set<T> join(Collection<T>... sets) {
        return join(new HashSet<T>(), sets);
    }

    public static String native2AscII(String str) {
        if (str == null) return null;
        char[] charPoints = str.toCharArray();
        StringBuffer strBuf = new StringBuffer();
        for (char ch : charPoints) {
            if (ch < 256)
                strBuf.append(ch);
            else
                strBuf.append("\\u").append(Integer.toHexString(ch));
        }
        return strBuf.toString();
    }

//    @Deprecated
//    public static File getFile(String fileName) throws IOException {
//        return getNewFile(fileName);
//    }

    public static File getNewFile(String fileName) throws IOException {
        File f = new File(fileName);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        if (f.exists())
            f.delete();
        f.createNewFile();
        return f;
    }

    public static URL getResource(String resource, ClassLoader... classLoaders) {
        resource = Common.trim(resource, '/');

        ClassLoader classLoader;
        URL url;

        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }

        if (classLoaders != null && classLoaders.length > 0) {
            for (ClassLoader cl : classLoaders) {
                url = cl.getResource(resource);
                if (url != null) {
                    return url;
                }
            }
        }

        // We could not find resource. Ler us now try with the
        // classloader that loaded this class.
        classLoader = Common.class.getClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }
        return ClassLoader.getSystemResource(resource);
    }

    public static <T> int indexOf(T[] array, T t) {
        if (array == null) throw new NullPointerException("indexOf: array must not be NULL.");
        for (int i = 0; i < array.length; i++) {
            if (t == null) {
                if (array[i] == null) return i;
            } else {
                if (t.equals(array[i])) return i;
            }
        }
        return -1;
    }

    public static String concat(List<String> list, String split) {
        if (list == null) return null;
        switch (list.size()) {
            case 0:
                return "";
            case 1:
                return list.get(0);
            default:
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) builder.append(split);
                    builder.append(list.get(i));
                }
                return builder.toString();
        }
    }

    public static <T> T to(String str, T value) {
        if (value == null) {
            if (str == null)
                return null;
            throw new NullPointerException("value is null.");
        }
        Class cls = value.getClass();
        if (cls.equals(String.class)) {
            //noinspection unchecked
            return (T) (str == null ? value : str);
        }
        if (cls.isArray() && cls.getComponentType().equals(String.class)) {
            //noinspection unchecked
            return (T) toArray(str, ",", (String[]) value);
        }
        StringConvertWithDefaultValue defaultValue = converterServiceLoader.get().getServiceInstance(cls);
        if (defaultValue == null) {
            throw new RuntimeException("String to " + cls + " is not supported.");
        }
        //noinspection unchecked
        return (T) defaultValue.convertTo(str, value, cls);
    }

    public static int toInt(String str, int value) {
        try {
            return Integer.valueOf(str);
        } catch (Throwable th) {
            return value;
        }
    }

    public static long toLong(String str, long value) {
        try {
            return Long.valueOf(str);
        } catch (Throwable th) {
            return value;
        }
    }

    public static boolean toBool(String str, boolean v) {
        String s = nullToStr(str);
        if (s.equals("1") || s.equalsIgnoreCase("T")
                || s.equalsIgnoreCase("TRUE"))
            return true;
        else if (s.equals("0") || s.equalsIgnoreCase("F")
                || s.equalsIgnoreCase("FALSE"))
            return false;
        else
            return v;
    }

    public static List<String> toArray(String str, String delim, List<String> v) {
        if (str == null)
            return v;
        StringTokenizer st = new StringTokenizer(str, delim, false);
        List<String> list = new ArrayList<String>();
        while (st.hasMoreElements()) {
            list.add(st.nextToken().trim());
        }
        return list;
    }

    public static String[] toArray(String str, String delim, String[] v) {
        List<String> list = toArray(str, delim, v == null ? null : Arrays.asList(v));
        return list == null ? null : list.toArray(new String[0]);
//        return toArray(str, delim, v == null ? new ArrayList<String>() : Arrays.asList(v))
//                .toArray(new String[0]);
    }

    private static boolean inArray(char ch, char[] chars) {
        for (char c : chars) {
            if (c == ch) return true;
        }
        return false;
    }

    public static String trim(String str, char... trimChars) {
        if (Common.isBlank(str) || trimChars == null || trimChars.length == 0) return str;
        char[] chars = str.toCharArray();
        int start, end = chars.length;
        for (start = 0; start < end; start++) {
            if (!inArray(chars[start], trimChars)) break;
        }
        for (; end > start; end--) {
            if (!inArray(chars[end - 1], trimChars)) break;
        }
        return new String(chars, start, end - start);
    }

    public static String trim(String str, String toTrim) {
        if (Common.isBlank(str) || Common.isBlank(toTrim)) return str;
        return trim(str, toTrim.toCharArray());
    }

    public static boolean sameString(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }

    /**
     * 参考：http://tools.jb51.net/table/gb2312
     *
     * @return 一个随机的中文字符(GB2312的一级文字)
     */
    public static char randomGB2312Char() {
        // 16-55区(0xB0 - 0xD7, D7最大到F9): 一级汉字
        // 56-87区(0xD8 - 0xF7): 二级汉字
        try {
            int b1 = Common.random(0xB0, 0xD7);
            int b2 = Common.random(0xA1, b1 == 0xD7 ? 0xF9 : 0xFE);
            return new String(new byte[]{(byte) b1, (byte) b2}, "GB2312").charAt(0);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public static char randomChar(String s) {
        if (Common.isBlank(s)) throw new IllegalArgumentException("range is blank.");
        return s.charAt(Common.random(s.length()));
    }

    public static <T> T random(T[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length)];
    }

    public static byte random(byte[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length)];
    }

    public static short random(short[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length)];
    }

    public static int random(int[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length)];
    }

    public static long random(long[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length)];
    }

    public static float random(float[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length)];
    }

    public static double random(double[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length)];
    }

    public static String randomStr(int min, int max, String range) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, j = random(min, max); i < j; i++) {
            builder.append(randomChar(range));
        }
        return builder.toString();
    }

    public static boolean isSameStr(String s1, String s2) {
        if (s1 == s2) return true;
        if (s1 == null || s2 == null) return false;
        return s1.equals(s2);
    }

    public static Calendar copy(Calendar calendar) {
        return calendar == null ? null : (Calendar) calendar.clone();
    }

    public static String lowerFirstChar(String string) {
        if (string == null) return string;
        char[] charSeq = string.toCharArray();
        if (charSeq.length > 0 && charSeq[0] >= 'A' && charSeq[0] <= 'Z') {
            charSeq[0] = (char) (charSeq[0] + TO_LOWER);
            return new String(charSeq);
        }
        return string;
    }

    public static String upperFirstChar(String string) {
        if (string == null) return string;
        char[] charSeq = string.toCharArray();
        if (charSeq.length > 0 && charSeq[0] >= 'a' && charSeq[0] <= 'z') {
            charSeq[0] = (char) (charSeq[0] - TO_LOWER);
            return new String(charSeq);
        }
        return string;
    }

    public static String camelCase(String s) {
        return camelCase(s, false);
    }

    public static String camelCase(String s, String delimiters) {
        return camelCase(s, false, delimiters);
    }

    public static String camelCase(String s, boolean firstCharUpperCase) {
        return camelCase(s, firstCharUpperCase, DEFAULT_DELIM);
    }

    public static String camelCase(String s, boolean firstCharUpperCase, String delimiters) {
        StringTokenizer st = new StringTokenizer(s, delimiters);
        StringBuilder builder = new StringBuilder();
        while (st.hasMoreElements()) {
            String node = st.nextToken();
            if (node.length() == 0) continue;
            builder.append(upperFirstChar(node));
        }
        return firstCharUpperCase ?
                upperFirstChar(builder.toString()) :
                lowerFirstChar(builder.toString());
    }

    /**
     * http://www.cnblogs.com/yujunyong/articles/2004724.html
     *
     * @param strA
     * @param strB
     * @return
     */
    private static int calculateStringDistance(String strA, String strB) {
        int lenA = strA.length();
        int lenB = strB.length();
        int[][] c = new int[lenA + 1][lenB + 1];
        // Record the distance of all begin points of each string
        // 初始化方式与背包问题有点不同
        for (int i = 0; i < lenA; i++) c[i][lenB] = lenA - i;
        for (int j = 0; j < lenB; j++) c[lenA][j] = lenB - j;
        c[lenA][lenB] = 0;
        for (int i = lenA - 1; i >= 0; i--)
            for (int j = lenB - 1; j >= 0; j--) {
                if (strB.charAt(j) == strA.charAt(i))
                    c[i][j] = c[i + 1][j + 1];
                else
                    c[i][j] = Math.min(Math.min(c[i][j + 1], c[i + 1][j]), c[i + 1][j + 1]) + 1;
            }

        return c[0][0];
    }

    /**
     * 两个字符串的相似度
     *
     * @param s1
     * @param s2
     * @return
     */
    public static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0f;
        if (s1.equals(s2)) return 1.0f;
        return 1.0f - calculateStringDistance(s1, s2) / (Math.max(s1.length(), s2.length()) * 1.0f);
    }

    public static <T> Map<String, T> subMap(String prefix, Map<String, T> map) {
        Map<String, T> subMap = new HashMap<String, T>();
        String prefixKey = prefix.endsWith(".") ? prefix : (prefix + '.');
        int length = prefixKey.length();
        for (String key : map.keySet()) {
            if (key.startsWith(prefixKey) && key.length() > length) {
                subMap.put(key.substring(length), map.get(prefixKey));
            }
        }
        return subMap;
    }

    public static String calendarToStr(Calendar calendar, String format) {
        return dateToStr(calendar.getTime(), format);
    }

    public static String calendarToStr(Calendar calendar) {
        return calendarToStr(calendar, DEFAULT_DATETIME_FORMAT);
    }

    public static String dateToStr(Date date, String format) {
        return getSafetyDateFormat(format).format(date);
    }

    public static String dateToStr(Date date) {
        return dateToStr(date, DEFAULT_DATETIME_FORMAT);
    }

    public static DateFormat getSafetyDateFormat(String format) {
        if (threadLocal.get() == null) {
            threadLocal.set(new SingletonMap<String, DateFormat>(
                    new SingletonMap.Builder<String, DateFormat>() {
                        @Override
                        public DateFormat build(String key) {
                            return new SimpleDateFormat(key);
                        }
                    }
            ));
        }
        return threadLocal.get().get(format);
    }

    public static Date strToDate(String str, String format) throws ParseException {
        return getSafetyDateFormat(format).parse(str);
    }

    public static Date strToDate(String str) throws ParseException {
        return strToDate(str, DEFAULT_DATETIME_FORMAT);
    }

    public static Calendar strToCalendar(String str, String format) throws ParseException {
        return dateToCalendar(strToDate(str, format));
    }

    public static String longToDateStr(long l) {
        return longToDateStr(l, DEFAULT_DATETIME_FORMAT);
    }

    public static String longToDateStr(long l, String format) {
        return calendarToStr(longToCalendar(l), format);
    }

    public static Calendar longToCalendar(long l) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(l);
        return calendar;
    }

    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static String now() {
        return now(DEFAULT_DATETIME_FORMAT);
    }

    public static String now(String format) {
        return dateToStr(Clock.getCalendar().getTime(), format);
    }

    public static RuntimeException runtimeException(Throwable th) {
        return th instanceof RuntimeException ? (RuntimeException) th : new RuntimeException(th.getLocalizedMessage(), th);
    }

    public static class StringToFloat implements StringConvertWithDefaultValue {

        @Override
        public Object convertTo(String str, Object defaultValue, Class<?> type) {
            try {
                return Float.parseFloat(str);
            } catch (Throwable t) {
                return defaultValue;
            }
        }

        @Override
        public boolean accept(Class<?> param) {
            return float.class.equals(param) || Float.class.equals(param);
        }
    }

    public static class StringToInt implements StringConvertWithDefaultValue {

        @Override
        public Object convertTo(String str, Object defaultValue, Class<?> type) {
            return toInt(str, (Integer) defaultValue);
        }

        @Override
        public boolean accept(Class<?> param) {
            return int.class.equals(param) || Integer.class.equals(param);
        }
    }

    public static class StringToLong implements StringConvertWithDefaultValue {

        @Override
        public Object convertTo(String str, Object defaultValue, Class<?> type) {
            return toLong(str, (Long) defaultValue);
        }

        @Override
        public boolean accept(Class<?> param) {
            return long.class.equals(param) || Long.class.equals(param);
        }
    }

    public static class StringToBoolean implements StringConvertWithDefaultValue {

        @Override
        public Object convertTo(String str, Object defaultValue, Class<?> type) {
            return toBool(str, (Boolean) defaultValue);
        }

        @Override
        public boolean accept(Class<?> param) {
            return boolean.class.equals(param) || Boolean.class.equals(param);
        }
    }

    public static Long getSystemStart() {
        return ManagementFactory.getRuntimeMXBean().getStartTime();
    }


    private static boolean isMatch(Pattern p, String str){
        return p.matcher(str).find();
    }

    public interface Processor {
        void process(URL resource, String resourceName);
    }


    private static class PathPattern {
        private Pattern pattern;
        private String path;
        private String originalPath;

        public PathPattern(String path) {
            this.originalPath = path;
            this.pattern = Pattern.compile(
                    "^" + Common.trim(path).replaceAll("\\.", "\\\\.")
                            .replaceAll("\\*{2,}", ".+")// 两个以上*匹配任意字符
                            .replaceAll("\\*", "[^/]+")
                            + ".+"
            );
            this.path = pathRoot(path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PathPattern)) return false;

            PathPattern that = (PathPattern) o;

            return originalPath.equals(that.originalPath);
        }

        @Override
        public int hashCode() {
            return originalPath.hashCode();
        }
    }

}
