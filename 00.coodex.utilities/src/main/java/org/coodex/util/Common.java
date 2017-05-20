/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

/**
 *
 */
package org.coodex.util;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * @author davidoff
 */
public class Common {

    public static final String LINE_SEPARATOR = System
            .getProperty("line.separator");
    public static final String PATH_SEPARATOR = System
            .getProperty("path.separator");
    public static final String FILE_SEPARATOR = System
            .getProperty("file.separator");

    public static <T> Set<T> arrayToSet(T[] array) {
        Set<T> set = new HashSet<T>();
        for (T t : array) {
            set.add(t);
        }
        return set;
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

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCopy(T object)
            throws IOException, ClassNotFoundException {
        // 序列化obj
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(object);
        } finally {
            oos.close();
        }

        // 反序列化成一个clone对象
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        try {
            return (T) ois.readObject();
        } finally {
            ois.close();
        }

    }

    public static int random(int max) {
        return random(0, max);
    }

    public static int random(int min, int max) {
        if (max == Integer.MAX_VALUE)
            max = max - 1;
        return min + (int) (Math.random() * (max - min + 1));
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
        int cached = -1;
        long start = Calendar.getInstance().getTimeInMillis();
        long wrote = 0;
        while ((cached = is.read(buf)) > 0) {
            // 自上次计时开始，已写入数据超出bps
            if (wrote >= bps) {
                long n = wrote / bps;
                wrote = wrote % bps;
                long interval = Calendar.getInstance().getTimeInMillis() - start;
                try {
                    if (interval < 1000 * n)
                        Thread.sleep(interval);
                } catch (InterruptedException e) {
                }
            }
            // 重新开始计时
            start = Calendar.getInstance().getTimeInMillis();
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
        String hs = "";
        for (int n = 0; n < b.length; n++) {
            String sTmp = Integer.toHexString(b[n] & 0XFF);
            if (sTmp.length() == 1) {
                hs = hs + "0" + sTmp;
            } else {
                hs = hs + sTmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * 交集
     *
     * @param set1
     * @param set2
     * @return
     */
    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<T>();
        result.addAll(set1);
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
        Set<T> result = new HashSet<T>();
        result.addAll(org);
        result.removeAll(todiv);
        return result;
    }

    private static <T, C extends Collection<T>> C join(C instance, Collection ... collections){
        if(collections != null && collections.length > 0){
            for(Collection c : collections){
                if(c != null) instance.addAll(c);
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
    public static <T> Set<T> join(Collection<T> ... sets) {
//        Set<T> result = new HashSet<T>();
//        if(sets != null && sets.length > 0){
//            for(Set<T> set : sets){
//                if(set != null) result.addAll(set);
//            }
//        }
//        result.addAll(ary1);
//        result.addAll(ary2);
        return join(new HashSet<T>(), sets);
    }

//    public static <T> List<T> joinList(List<T> ... lists) {
////        List<T> result = new ArrayList<T>();
////        if(lists != null && lists.length > 0){
////            for(List<T> set : lists){
////                if(set != null) result.addAll(set);
////            }
////        }
////        result.addAll(ary1);
////        result.addAll(ary2);
//        return join(new ArrayList<T>(), lists);
//    }


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

    @Deprecated
    public static File getFile(String fileName) throws IOException {
//        File f = new File(fileName);
//        if (!f.getParentFile().exists()) {
//            f.getParentFile().mkdirs();
//        }
//        if (f.exists())
//            f.delete();
//        f.createNewFile();
//        return f;
        return getNewFile(fileName);
    }

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
        ClassLoader classLoader = null;
        URL url = null;

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

    public static String[] toArray(String str, String delim, String[] v) {
        if (str == null)
            return v;
        StringTokenizer st = new StringTokenizer(str, delim, false);
        List<String> list = new ArrayList<String>();
        while (st.hasMoreElements()) {
            list.add(st.nextToken().trim());
        }
        return list.toArray(new String[0]);
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
        return s.charAt(Common.random(s.length() - 1));
    }

    public static <T> T random(T[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length - 1)];
    }

    public static byte random(byte[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length - 1)];
    }

    public static short random(short[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length - 1)];
    }

    public static int random(int[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length - 1)];
    }

    public static long random(long[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length - 1)];
    }

    public static float random(float[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length - 1)];
    }

    public static double random(double[] range) {
        if (range == null || range.length == 0) throw new IllegalArgumentException("range is blank.");
        return range[random(range.length - 1)];
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
}
