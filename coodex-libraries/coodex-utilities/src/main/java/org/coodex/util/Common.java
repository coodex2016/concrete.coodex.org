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

import org.coodex.id.IDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * @author davidoff
 */
@SuppressWarnings("unused")
public class Common {

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final String USER_DIR = System.getProperty("user.dir");

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public static final String DEFAULT_DATETIME_FORMAT = DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT;

    public static final Long SYSTEM_START_TIME = ManagementFactory.getRuntimeMXBean().getStartTime();

    public static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final static Logger log = LoggerFactory.getLogger(Common.class);

    private static final int TO_LOWER = 'a' - 'A';

    private final static String DEFAULT_DELIM = ".-_ /\\";


    private static final ThreadLocal<SingletonMap<String, DateFormat>> threadLocal = new ThreadLocal<>();

    private static final SelectableServiceLoader<Class<?>, StringConvertWithDefaultValue> converterServiceLoader
            = new LazySelectableServiceLoader<Class<?>, StringConvertWithDefaultValue>() {
    };
    private static final char[] BASE16_CHAR = "0123456789abcdef".toCharArray();

    private Common() {
    }

    private static long i2l(int i) {
        return i & 0xFFFFFFFFL;
    }

    public static boolean isWindows() {
        return PATH_SEPARATOR.equals(";");
    }

    private static String[] userDir() {
        return USER_DIR.split(isWindows() ? "\\\\" : "/");
    }

    public static String toAbsolutePath(String path) {
        return isWindows() ? toAbsolutePathWindows(path) : toAbsolutePathUnixLike(path);
    }

    private static String toAbsolutePathUnixLike(String path) {
        String[] pathNodes = path.replace('\\', '/').split("/");
        if (pathNodes.length > 0 && Common.isBlank(pathNodes[0])) return path;

        return getAbsolutePath(pathNodes);
    }

    private static String toAbsolutePathWindows(String path) {
        String[] pathNodes = path.replace('/', '\\').split("\\\\");
        if (pathNodes.length > 0 && pathNodes[0].indexOf(':') > 0) // "x:"开头表示绝对路径
            return path;
        return getAbsolutePath(pathNodes);
    }

    private static String getAbsolutePath(String[] pathNodes) {
        String[] userDirNodes = userDir();
        int i = 0, userDirNodesIndex = userDirNodes.length - 1;
        if (Common.isBlank(pathNodes[0])) {
            StringJoiner joiner = new StringJoiner(FILE_SEPARATOR);
            joiner.add(userDirNodes[0]);
            for (int j = 1; j < pathNodes.length; j++) {
                joiner.add(pathNodes[j]);
            }
            return joiner.toString();
        }
        while (i < pathNodes.length) {
            if (pathNodes[i].equals(".")) {
                i++;
                continue; // 当前目录
            }

            if (pathNodes[i].equals("..")) { // 上一级目录
                userDirNodesIndex = Math.max(0, userDirNodesIndex - 1);
                i++;
                continue;
            }
            break;
        }
        StringJoiner joiner = new StringJoiner(FILE_SEPARATOR);
        for (int x = 0; x <= userDirNodesIndex; x++) {
            joiner.add(userDirNodes[x]);
        }
        for (int x = i; i < pathNodes.length; i++) {
            joiner.add(pathNodes[i]);
        }
        return joiner.toString();
    }

    public static <T> Set<T> arrayToSet(T[] array) {
        return Arrays.stream(Objects.requireNonNull(array, "array MUST NOT null"))
                .collect(Collectors.toSet());
    }

    /**
     * 作为id时，使用{@link IDGenerator#newId()}替代
     * <p>
     * 获取uuid时，使用{@link UUIDHelper#getUUIDString()}
     *
     * @return uuid
     */
    @Deprecated
    public static String getUUIDStr() {
        return UUIDHelper.getUUIDString();
    }

    public static String sha1(String content) {
        return sha1(content, StandardCharsets.UTF_8);
    }

    public static String sha1(String content, Charset charset) {
        byte[] buf = (content == null) ? new byte[0] : content.getBytes(charset);
        return DigestHelper.sha1(buf);
    }

    public static <T> boolean inArray(T el, T[] array) {
        return indexOf(array, el) >= 0;
    }

    /**
     * @deprecated {@link #indexOf(Object[], Object)}
     */
    @Deprecated
    public static <T> int findInArray(T el, T[] array) {
//        for (int i = 0; i < array.length; i++) {
//            T t = array[i];
//            if (Objects.equals(t, el)) {
//                return i;
//            }
//        }
//        return -1;
        return indexOf(array, el);
    }

    public static String nullToStr(String str) {
        return str == null ? "" : str;
    }

    public static byte[] serialize(Object object) throws IOException {
        // 序列化obj
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            return bos.toByteArray();
        }
    }

    public static Object deserialize(byte[] buf) throws IOException, ClassNotFoundException {
        // 反序列化成一个clone对象
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf))) {
            return ois.readObject();
        }
    }

    public static <T extends Serializable> T deepCopy(T object)
            throws IOException, ClassNotFoundException {
        return cast(deserialize(serialize(object)));
    }

    /**
     * @param max 正整数
     * @return [0, max)的随机数
     */
    public static int random(int max) {
        return (int) random(0L, i2l(max));
    }

    /**
     * @param max 非负正数
     * @return [0, max]的随机数
     */
    public static int randomC(int max) {
        return (int) random(0L, max + 1L);
    }

    public static int random(int min, int max) {
        return (int) random(i2l(min), i2l(max));
    }

    public static int randomC(int min, int max) {
        return (int) random(i2l(min), max + 1L);
    }

    public static long random(long min, long max) {
//        if (min == max) return min;
//        float _min = Math.min(min, max);
//        float _max = Math.max(min, max);
//
//        return (long) (_min + Math.random() * (_max - _min));
        return random(min, max, false);
    }

    public static long random(long max) {
        return random(0, max);
    }

    public static long randomC(long max) {
        return random(0, max, true);
    }

    public static long randomC(long min, long max) {
        return random(min, max, true);
    }

    private static long random(long bound1, long bound2, boolean includeMax) {
        long min = Math.min(bound1, bound2);
        long max = Math.max(bound1, bound2);
        if (min == max) return min;
        long step = max - min + (includeMax ? 1L : 0L);
        if (step > 0)
            return min + (long) (Math.random() * step);
        // 越界情况，按照正负数的权重算一次随机判定落在哪一边
        return Math.random() < (Math.abs(min) * 1d / max) ?
                random(min, 0, includeMax) :
                random(0, max, includeMax);

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
            } catch (ClassNotFoundException | IOException e) {
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

    /**
     * @deprecated 使用 {@link Objects#requireNonNull(Object, String)} 替代
     */
    @Deprecated
    public static void checkNull(Object o, String msg) {
        Objects.requireNonNull(o, msg);
    }

    private static <T extends Comparable<T>> T _max(T c1, T c2) {
        return c1.compareTo(c2) >= 0 ? c1 : c2;
    }

    /**
     * @deprecated 使用 {@link Edge#between(Comparable, Comparable, Comparable)}替代
     */
    @Deprecated
    public static <T extends Comparable<T>> boolean between(T t, T bound1, T bound2) {
        return Edge.OPEN_OPEN.between(t, bound1, bound2);
    }

    @SafeVarargs
    public static <T extends Comparable<T>> T max(T c1, T c2, T... others) {
        Objects.requireNonNull(c1, "c1 is null");
        Objects.requireNonNull(c2, "c2 is null");
        T currentMax = _max(c1, c2);
        if (others != null && others.length > 0) {
            for (int i = 0, len = others.length; i < len; i++) {
//                int finalI = i;
//                checkNull(others[i], () -> "c" + (finalI + 3) + " is null");
                Objects.requireNonNull(others[i], "c" + (i + 3) + " is null");
                currentMax = _max(currentMax, others[i]);
            }
        }
        return currentMax;
    }

    private static <T extends Comparable<T>> T _min(T c1, T c2) {
        return c1.compareTo(c2) <= 0 ? c1 : c2;
    }

    @Deprecated
    public static String byte2hex(byte[] b) {
        return base16Encode(b);
    }

    @Deprecated
    public static String byte2hex(byte[] b, int offset, int length) {
        return base16Encode(b, offset, length);
    }

    @Deprecated
    public static String byte2hex(byte[] b, int col, String split) {
        return base16Encode(b, col, split);
    }

    @Deprecated
    public static String byte2hex(byte[] b, int offset, int length, int col, String split) {
        return base16Encode(b, offset, length, col, split);
    }

    /**
     * @param b 需要编码的字节数组
     * @return 编码后的字符串
     */
    public static String base16Encode(byte[] b) {
        return base16Encode(b, 0, b.length);
    }

    /**
     * @param b      需要编码的字节数组
     * @param offset 编码字节数组偏移
     * @param length 编码字节数组长度
     * @return 编码后的字符串
     */
    public static String base16Encode(byte[] b, int offset, int length) {
        return base16Encode(b, offset, length, Integer.MAX_VALUE, null);
    }

    /**
     * @param b     需要编码的字节数组
     * @param col   每个字节编码后算一列，此参数用来指定编码后每行多少列，行与行之间使用系统变量line.separator来分隔
     * @param split 列于列之间的分隔字符传串，行首行尾不加
     * @return 编码后的字符串
     */
    public static String base16Encode(byte[] b, int col, String split) {
        return base16Encode(b, line -> col, split);
    }

    /**
     * @param b           需要编码的字节数组
     * @param colFunction 每个字节编码后算一列，此参数用来指定编码后各行多少列，行与行之间使用系统变量line.separator来分隔
     * @param split       列于列之间的分隔字符传串，行首行尾不加
     * @return 编码后的字符串
     */
    public static String base16Encode(byte[] b, Function<Integer, Integer> colFunction, String split) {
        return base16Encode(b, 0, b.length, colFunction, split);
    }

    /**
     * @param b      需要编码的字节数组
     * @param offset 编码字节数组偏移
     * @param length 编码字节数组长度
     * @param col    每个字节编码后算一列，此参数用来指定编码后每行多少列，行与行之间使用系统变量line.separator来分隔
     * @param split  列于列之间的分隔字符传串，行首行尾不加
     * @return 编码后的字符串
     */
    public static String base16Encode(byte[] b, int offset, int length, int col, String split) {
        return base16Encode(b, offset, length, line -> col, split);
    }

    private static String encodeByte(byte b) {
        int temp = b & 0xFF;
        return new String(new char[]{
                BASE16_CHAR[(temp >> 4) & 0xF],
                BASE16_CHAR[temp & 0xF]
        });
    }

    /**
     * @param b           需要编码的字节数组
     * @param offset      编码字节数组偏移
     * @param length      编码字节数组长度
     * @param colFunction 每个字节编码后算一列，此参数用来指定编码后各行多少列，行与行之间使用系统变量line.separator来分隔
     * @param split       列于列之间的分隔字符传串，行首行尾不加
     * @return 编码后的字符串
     */
    public static String base16Encode(byte[] b, int offset, int length, Function<Integer, Integer> colFunction, String split) {
        StringBuilder builder = new StringBuilder();
        boolean blankSplit = split == null || "".equals(split);
        int line = 0;
        int index = offset;
        int remain = length;
        while (remain > 0) {
            if (line > 0) builder.append(LINE_SEPARATOR);//非首行则添加行隔符
            int col = colFunction.apply(line++);// 计算当前行列数
            int encodeCountForThisLine = Math.min(remain, col);//在剩余字节数和当前行列数中算出本行要编码的字节数
            remain -= encodeCountForThisLine;// 预减掉本行编码数
            boolean firstByte = true;
            while (encodeCountForThisLine > 0) {
                encodeCountForThisLine--;

                if (!firstByte && !blankSplit) {
                    builder.append(split);
                } else {
                    firstByte = false;
                }
                builder.append(encodeByte(b[index++]));
            }
        }
        return builder.toString();
//        Byte[] bytes = new Byte[b.length];
//        Arrays.setAll(bytes, n -> b[n]);
//        return formatArray(bytes, offset, length, colFunction, split, Common::encodeByte);
    }

    public static <T> String formatArray(
            T[] array, int offset, int length,
            Function<Integer, Integer> colFunction,
            String split, Function<T, String> encoder) {
        StringBuilder builder = new StringBuilder();
        boolean blankSplit = split == null || "".equals(split);
        int line = 0;
        int index = offset;
        int remain = length;
        while (remain > 0) {
            if (line > 0) builder.append(LINE_SEPARATOR);//非首行则添加行隔符
            int col = colFunction.apply(line++);// 计算当前行列数
            int encodeCountForThisLine = Math.min(remain, col);//在剩余字节数和当前行列数中算出本行要编码的字节数
            remain -= encodeCountForThisLine;// 预减掉本行编码数
            boolean firstByte = true;
            while (encodeCountForThisLine > 0) {
                encodeCountForThisLine--;

                if (!firstByte && !blankSplit) {
                    builder.append(split);
                } else {
                    firstByte = false;
                }
                builder.append(encoder.apply(array[index++]));
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

    @Deprecated
    public static byte[] hex2byte(String hexString) {
        return base16Decode(hexString);
    }

    @Deprecated
    public static byte[] hex2byte(String hexString, String ignoreChars) {
        return base16Decode(hexString, ignoreChars);
    }

    @SuppressWarnings("unused")
    public static byte[] base16Decode(String hexString) {
        return base16Decode(hexString, LINE_SEPARATOR + " ");
    }

    public static byte[] base16Decode(String hexString, String ignoreChars) {
        char[] ignore = ignoreChars == null ? new char[0] : ignoreChars.toCharArray();
        int hi = -1, low = -1;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean closed;
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
                closed = hi != -1;
            }

            if (closed && low != -1) {
                byteArrayOutputStream.write(hi == -1 ? low : ((hi << 4 | low)));
                hi = -1;
                low = -1;
            }
        }
        if (low != -1) {
            //noinspection ConstantConditions
            byteArrayOutputStream.write(hi == -1 ? low : ((hi << 4 | low)));
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 交集
     *
     * @param set1 set1
     * @param set2 set2
     * @return 交集
     */
    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }

    /**
     * 差集 org - todiv
     *
     * @param org   org
     * @param todiv todiv
     * @return 差集 org - todiv
     */
    public static <T> Set<T> difference(Set<T> org, Set<T> todiv) {
        Set<T> result = new HashSet<>(org);
        result.removeAll(todiv);
        return result;
    }

    @SafeVarargs
    private static <T, C extends Collection<T>> C join(C instance, Collection<? extends T>... collections) {
        if (collections != null && collections.length > 0) {
            for (Collection<? extends T> c : collections) {
                if (c != null) instance.addAll(c);
            }
        }
        return instance;
    }

    /**
     * 并集
     *
     * @param sets sets
     * @return 并集
     */
    @SafeVarargs
    public static <T> Set<T> join(Collection<T>... sets) {
        return join(new HashSet<>(), sets);
    }

    public static String native2AscII(String str) {
        if (str == null) return null;
        char[] charPoints = str.toCharArray();
        StringBuilder strBuf = new StringBuilder();
        for (char ch : charPoints) {
            if (ch < 256)
                strBuf.append(ch);
            else
                strBuf.append("\\u").append(Integer.toHexString(ch));
        }
        return strBuf.toString();
    }

    @Deprecated
    public static File getNewFile(String fileName) throws IOException {
        return newFile(fileName);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File newFile(String fileName) throws IOException {
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

        URL url;
        for (String path : ResourceScanner.getExtraResourcePath()) {
            String filePath = path + FILE_SEPARATOR + resource;
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    return new URL(filePath);
                } catch (MalformedURLException ignore) {
                }
            }
        }

        ClassLoader classLoader;

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

    public static String concat(Collection<String> list, String split) {
        if (list == null) return null;
        switch (list.size()) {
            case 0:
                return "";
            case 1:
                return list.iterator().next();
            default:
                StringJoiner joiner = new StringJoiner(split);
                list.forEach(joiner::add);
                return joiner.toString();
//                StringBuilder builder = new StringBuilder();
//                for (int i = 0; i < list.size(); i++) {
//                    if (i > 0) builder.append(split);
//                    builder.append(list.get(i));
//                }
//                return builder.toString();
        }
    }

//    @Deprecated
//    public static File getFile(String fileName) throws IOException {
//        return getNewFile(fileName);
//    }

    public static <T> T to(String str, T value) {
        if (value == null) {
            if (str == null)
                return null;
            throw new NullPointerException("value is null.");
        }
        Class<?> cls = value.getClass();
        if (cls.equals(String.class)) {
            return cast(str == null ? value : str);
        }
        if (cls.isArray() && cls.getComponentType().equals(String.class)) {
            return cast(toArray(str, ",", (String[]) value));
        }
        StringConvertWithDefaultValue defaultValue = converterServiceLoader.select(cls);
        if (defaultValue == null) {
            throw new RuntimeException("String to " + cls + " is not supported.");
        }
        return cast(defaultValue.convertTo(str, value, cls));
    }

    public static int toInt(String str, Supplier<Integer> valueSupplier) {
        try {
            return parseInt(str);
        } catch (Throwable th) {
            return valueSupplier.get();
        }
    }


//    public static <T> T to(String str, Supplier<T> defaultSupplier) {
//        if (defaultSupplier == null) {
//            if (str == null) return null;
//            throw new NullPointerException("defaultValue Supplier is null.");
//        }
//        return getT(str, GenericTypeHelper.typeToClass(
//                GenericTypeHelper.solveFromInstance(
//                        Supplier.class.getTypeParameters()[0],
//                        defaultSupplier
//                )
//        ), defaultSupplier);
//    }

    public static int toInt(String str, int value) {
        try {
            return parseInt(str);
        } catch (Throwable th) {
            return value;
        }
    }

    public static long toLong(String str, Supplier<Long> value) {
        try {
            return parseLong(str);
        } catch (Throwable th) {
            return value.get();
        }
    }

    public static long toLong(String str, long value) {
        try {
            return parseLong(str);
        } catch (Throwable th) {
            return value;
        }
    }

    @SuppressWarnings("unused")
    public static boolean toBool(String str, Supplier<Boolean> v) {
        String s = nullToStr(str);
        if (s.equals("1") || s.equalsIgnoreCase("T")
                || s.equalsIgnoreCase("TRUE"))
            return true;
        else if (s.equals("0") || s.equalsIgnoreCase("F")
                || s.equalsIgnoreCase("FALSE"))
            return false;
        else
            return v.get();
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
        if (Common.isBlank(str))
            return v;
        StringTokenizer st = new StringTokenizer(str, delim, false);
        List<String> list = new ArrayList<>();
        while (st.hasMoreElements()) {
            list.add(st.nextToken().trim());
        }
        return list;
    }

    public static String[] toArray(String str, String delim, Supplier<String[]> v) {
        List<String> list = toArray(str, delim, (List<String>) null);
        return list == null ? v.get() : list.toArray(new String[0]);
//        return toArray(str, delim, v == null ? new ArrayList<String>() : Arrays.asList(v))
//                .toArray(new String[0]);
    }

    public static String[] toArray(String str, String delim, String[] v) {
        List<String> list = toArray(str, delim, (List<String>) null);
        return list == null ? v : list.toArray(new String[0]);
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

    @Deprecated
    public static boolean sameString(String str1, String str2) {
//        if (str1 == null && str2 == null) return true;
//        if (str1 == null || str2 == null) return false;
//        return str1.equals(str2);
        return Objects.equals(str1, str2);
    }

    /**
     * https://zh.wikipedia.org/wiki/GB_2312
     *
     * @return 一个随机的中文字符(GB2312的一级文字)
     */
    public static char randomGB2312Char() {
        // 16-55区(0xB0 - 0xD7, D7最大到F9): 一级汉字
        // 56-87区(0xD8 - 0xF7): 二级汉字
        try {
            int b1 = randomC(0xB0, 0xD7);
            int b2 = randomC(0xA1, b1 == 0xD7 ? 0xF9 : 0xFE);
            return new String(new byte[]{(byte) b1, (byte) b2}, "GB2312").charAt(0);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public static char randomChar(String s) {
        if (Common.isBlank(s)) throw new IllegalArgumentException("range is blank.");
        return s.charAt(Common.random(s.length()));
    }

//    /**
//     * https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97%E5%86%85%E7%A0%81%E6%89%A9%E5%B1%95%E8%A7%84%E8%8C%83
//     *
//     * @return 一个随机的GBK字符
//     */
//    public static char randomGBKChar(){
//
//    }

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

    public static Calendar copy(Calendar calendar) {
        return calendar == null ? null : (Calendar) calendar.clone();
    }

    public static String lowerFirstChar(String string) {
        if (string == null) return null;
        char[] charSeq = string.toCharArray();
        if (charSeq.length > 0 && charSeq[0] >= 'A' && charSeq[0] <= 'Z') {
            charSeq[0] = (char) (charSeq[0] + TO_LOWER);
            return new String(charSeq);
        }
        return string;
    }

    public static String upperFirstChar(String string) {
        if (string == null) return null;
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
     * @param strA strA
     * @param strB strB
     * @return string distance from strA to strB
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
     * @param s1 s1
     * @param s2 s2
     * @return 两个字符串的相似度
     */
    public static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0d;
        if (s1.equals(s2)) return 1.0d;
        return 1.0d - calculateStringDistance(s1, s2) / (Math.max(s1.length(), s2.length()) * 1.0d);
    }

    public static <T> Map<String, T> subMap(String prefix, Map<String, T> map) {
        Map<String, T> subMap = new HashMap<>();
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
            threadLocal.set(SingletonMap.<String, DateFormat>builder().function(SimpleDateFormat::new).build());
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

    public static byte[] long2Bytes(long data) {
        return new byte[]{
                (byte) ((data >> 56) & 0xff),
                (byte) ((data >> 48) & 0xff),
                (byte) ((data >> 40) & 0xff),
                (byte) ((data >> 32) & 0xff),
                (byte) ((data >> 24) & 0xff),
                (byte) ((data >> 16) & 0xff),
                (byte) ((data >> 8) & 0xff),
                (byte) ((data) & 0xff),
        };
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

    @SafeVarargs
    public static <T extends Comparable<T>> T min(T c1, T c2, T... others) {
        Objects.requireNonNull(c1, "c1 is null");
        Objects.requireNonNull(c2, "c2 is null");
        T currentMin = _min(c1, c2);
        if (others != null && others.length > 0) {
            for (int i = 0, len = others.length; i < len; i++) {
                Objects.requireNonNull(others[i], "c" + (i + 3) + " is null");
                currentMin = _min(currentMin, others[i]);
            }
        }
        return currentMin;
    }

    public static Calendar calendar(int year) {
        return buildCalendar(year, 0, 1, 0, 0, 0, 0);
    }

    public static Calendar calendar(int year, int month) {
        return buildCalendar(year, month, 1, 0, 0, 0, 0);
    }

    public static Calendar calendar(int year, int month, int date) {
        return buildCalendar(year, month, date, 0, 0, 0, 0);
    }

    public static Calendar calendar(int year, int month, int date, int hour) {
        return buildCalendar(year, month, date, hour, 0, 0, 0);
    }

    public static Calendar calendar(int year, int month, int date, int hour, int minute) {
        return buildCalendar(year, month, date, hour, minute, 0, 0);
    }

    public static Calendar calendar(int year, int month, int date, int hour, int minute, int second) {
        return buildCalendar(year, month, date, hour, minute, second, 0);
    }

    public static Calendar calendar(int year, int month, int date, int hour, int minute, int second, int millisecond) {
        return buildCalendar(year, month, date, hour, minute, second, millisecond);
    }

    private static Calendar buildCalendar(int year, int month, int date, int hour, int minute, int second, int millisecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar;
    }

    @SuppressWarnings("fallthrough")
    public static Calendar truncate(Calendar calendar, int fromField) {
        Calendar result = (Calendar) calendar.clone();
        switch (fromField) {
            case Calendar.YEAR:
                result.set(Calendar.YEAR, 1970);
            case Calendar.MONTH:
                result.set(Calendar.MONTH, 0);
            case Calendar.DATE:
                result.set(Calendar.DATE, 1);
            case Calendar.HOUR:
            case Calendar.HOUR_OF_DAY:
                result.set(Calendar.HOUR_OF_DAY, 0);
            case Calendar.MINUTE:
                result.set(Calendar.MINUTE, 0);
            case Calendar.SECOND:
                result.set(Calendar.SECOND, 0);
            case Calendar.MILLISECOND:
                result.set(Calendar.MILLISECOND, 0);
            default:
        }
        return result;
    }

    public static String now() {
        return now(DEFAULT_DATETIME_FORMAT);
    }

    public static String now(String format) {
        return dateToStr(Clock.now().getTime(), format);
    }

    public static RuntimeException rte(Throwable th) {
        return th instanceof RuntimeException ? (RuntimeException) th : new RuntimeException(th.getLocalizedMessage(), th);
    }

    public static Long getSystemStart() {
        return SYSTEM_START_TIME;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static void sleep(long ms) {
        try {
            Clock.sleep(ms);
        } catch (InterruptedException e) {
            throw rte(e);
        }
    }


//    @Deprecated
//    public interface ResourceFilter extends BiFunction<String, String, Boolean> {
//        boolean accept(String root, String resourceName);
//
//        @Override
//        default Boolean apply(String root, String resourceName) {
//            return accept(root, resourceName);
//        }
//    }
//
//    @Deprecated
//    public interface Processor extends BiConsumer<URL, String> {
//        void process(URL url, String resourceName);
//
//        @Override
//        default void accept(URL url, String resourceName) {
//            process(url, resourceName);
//        }
//    }

    //    public static void checkNull(Object o, Supplier<String> supplier) {
//        if (o == null) throw new NullPointerException(supplier == null ? null : supplier.get());
//    }
    public enum Edge {
        OPEN_OPEN(0, 0),
        CLOSE_OPEN(-1, 0),
        OPEN_CLOSE(0, 1),
        CLOSE_CLOSE(-1, 1);

        private final int left;
        private final int right;

        Edge(int left, int right) {
            this.left = left;
            this.right = right;
        }

        public <T extends Comparable<T>> boolean between(T t, T bound1, T bound2) {
            Objects.requireNonNull(t, "t is null");
            Objects.requireNonNull(bound1, "bound1 is null");
            Objects.requireNonNull(bound2, "bound2 is null");
            return t.compareTo(_min(bound1, bound2)) > left && t.compareTo(_max(bound1, bound2)) < right;
        }
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


}
