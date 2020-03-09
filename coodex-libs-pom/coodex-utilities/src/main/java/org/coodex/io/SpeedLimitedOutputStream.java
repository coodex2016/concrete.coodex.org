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

package org.coodex.io;

import org.coodex.util.Clock;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by davidoff shen on 2016-12-15.
 */
public class SpeedLimitedOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final int speedLimit;
    private long lastCountTime = 0;
    private int wroteBytesFromLastCountTime = 0;
    public SpeedLimitedOutputStream(OutputStream outputStream, int speedLimit) {
        this.outputStream = outputStream;
        this.speedLimit = speedLimit;
    }

    private void startCount() {
        lastCountTime = Clock.currentTimeMillis();
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
            long toSleep = 1000l - (Clock.currentTimeMillis() - lastCountTime);
            if (toSleep > 0) {
                try {
                    Clock.sleep(toSleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
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
