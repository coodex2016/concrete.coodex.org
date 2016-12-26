/**
 *
 */
package cc.coodex.util;

import java.io.*;
import java.net.URL;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    /**
     * 并集 ary1 + ary2
     *
     * @param ary1
     * @param ary2
     * @return
     */
    public static <T> Set<T> join(Set<T> ary1, Set<T> ary2) {
        Set<T> result = new HashSet<T>();
        result.addAll(ary1);
        result.addAll(ary2);
        return result;
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


    public static URL getResource(String resource) {
        ClassLoader classLoader = null;
        URL url = null;

        classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
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

}
