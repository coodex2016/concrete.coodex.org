package cc.coodex.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by davidoff shen on 2016-12-15.
 */
public class SpeedLimitedOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final int speedLimit;

    public SpeedLimitedOutputStream(OutputStream outputStream, int speedLimit) {
        this.outputStream = outputStream;
        this.speedLimit = speedLimit;
    }


    private long lastCountTime = 0;
    private int wroteBytesFromLastCountTime = 0;

    private void startCount() {
        lastCountTime = System.currentTimeMillis();
        wroteBytesFromLastCountTime = 0;
    }

    // 检查是否超限
    private void check(int size) {
        if (speedLimit == Integer.MAX_VALUE) return;
        if (lastCountTime == 0) {
            startCount();
        }
        wroteBytesFromLastCountTime += size;

        if (wroteBytesFromLastCountTime >= speedLimit) {
            long toSleep = 1000l - (System.currentTimeMillis() - lastCountTime);
            if (toSleep > 0) {
                try {
                    Thread.sleep(toSleep);
                } catch (InterruptedException e) {
                }
            }
            startCount();
        }

    }


    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int toWrote = Math.min(speedLimit, len);
            outputStream.write(b, off, toWrote);
            check(toWrote);
            off += toWrote;
            len -= toWrote;
        }
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        check(1);
    }
}
