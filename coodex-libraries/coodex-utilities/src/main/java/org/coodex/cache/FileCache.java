package org.coodex.cache;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class FileCache implements Cache {
    private final static Logger log = LoggerFactory.getLogger(FileCache.class);
    private static final int MAX_FILE_SIZE = 256 * 1024;
    private final File dir;
    private final String node;
    private final String name;

    private final Index index = new Index();
    private final ReentrantLock lock = new ReentrantLock();

    public FileCache(String node, String name) {
        this.node = node;
        this.name = name;
        this.dir = new File(node + "/" + name);
        if (!dir.exists()) dir.mkdirs();
        index.load();
    }

    @Override
    public String getNode() {
        return node;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int cachedItems() {
        lock.lock();
        try {
            return index.cached;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public byte[] next() {
        lock.lock();
        try {
            if (index.cached == 0) return null;

            File file = file(index.readFile);
            if (!file.exists()) return null;

            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(index.readOffset);
                int length = raf.readUnsignedShort();
                byte[] data = new byte[length];
                raf.readFully(data);

                index.readOffset += 2 + length;
                index.cached--;

                // 如果文件读取完了
                if (index.readOffset >= raf.length()) {
                    if (index.readFile == index.writeFile) {
                        // 复用当前文件
                        index.readOffset = 0;
                        index.writeOffset = 0;
                        raf.close();
                        if (index.readFile != 0) {
                            index.readFile = index.writeFile = 0;
                            file.delete();
                        }
                        try (RandomAccessFile wraf = new RandomAccessFile(file(index.readFile), "rw")) {
                            wraf.setLength(0);
                        }
                    } else {
                        // 删除旧文件，推进索引
                        file.delete();
                        index.readFile = (index.readFile + 1) & 0xF_FF_FF_FF;
                        index.readOffset = 0;
                    }
                }

                index.save();
                return data;
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cache(byte[] bytes) {
        cache(bytes, 0, bytes.length);
    }

    @Override
    public void cache(byte[] data, int offset, int length) {
        if (length > 65536) throw new IllegalArgumentException("Data too large");

        lock.lock();
        try {
            File file = file(index.writeFile);
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(index.writeOffset);
                raf.writeByte((length >> 8) & 0xFF);
                raf.writeByte(length & 0xFF);
                raf.write(data, offset, length);

                index.writeOffset += 2 + length;
                index.cached++;

                if (index.writeOffset >= MAX_FILE_SIZE) {
                    index.writeFile = (index.writeFile + 1) & 0xF_FF_FF_FF; ;
                    index.writeOffset = 0;
                }

                index.save();
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    private File file(int id) {
        return new File(dir, String.format("%08X", id));
    }

    // ======================= Index =======================
    class Index {
        int readFile = 1, writeFile = 1;
        int readOffset = 0, writeOffset = 0;
        int cached = 0;

        void save() {
            File file = new File(FileCache.this.dir, "index");
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
                out.writeInt(readFile);
                out.writeInt(readOffset);
                out.writeInt(writeFile);
                out.writeInt(writeOffset);
                out.writeInt(cached);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void load() {
            File file = new File(FileCache.this.dir, "index");
            if (!file.exists()) return;
            try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
                readFile = in.readInt();
                readOffset = in.readInt();
                writeFile = in.readInt();
                writeOffset = in.readInt();
                cached = in.readInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}