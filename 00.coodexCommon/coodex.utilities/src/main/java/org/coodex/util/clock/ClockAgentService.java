/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.util.clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 基于 tcp 提供一个时间服务器
 */
public class ClockAgentService extends Thread {
    private final static Logger log = LoggerFactory.getLogger(ClockAgentService.class);

    public static int PORT = 0x1978 + 0x0730;

    private final int port;
    private final String host;
    private boolean listening = false;
    private DefaultClockAgent defaultClockAgent = new DefaultClockAgent();

    public ClockAgentService() {
        this("0.0.0.0", PORT);
    }

    public ClockAgentService(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            try {
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
                log.info("Clock Agent Service start [{}:{}]....", host, port);
                this.listening = true;
                Selector selector = Selector.open();
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                while (true) {
                    if(!this.listening) break;

                    if (selector.select(3000) == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isAcceptable()) {
                            SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
                            socketChannel.configureBlocking(false);
                            ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 4);// start/ baseline/ magnification
                            buffer.putLong(0, defaultClockAgent.getStart());
                            buffer.putLong(8, defaultClockAgent.getBaseLine());
                            buffer.putFloat(16, defaultClockAgent.getMagnification());
                            socketChannel.write(buffer);
                        }
                        keyIterator.remove();
                    }
                }
            } finally {
                serverSocketChannel.close();
                log.debug("Clock Agent Service shutdown.");
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    public void shutdown(){
        this.listening = false;
    }
}
