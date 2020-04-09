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

import org.coodex.config.Config;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.coodex.util.Singleton;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import static org.coodex.util.clock.ClockAgentService.PORT;

public class RemoteClockAgent extends AbstractClockAgent {

    public static final String KEY_REMOTE_HOST = Clock.class.getName() + ".remoteHost";
    public static final String KEY_REMOTE_PORT = Clock.class.getName() + ".remotePort";

    static class Configuration {
        Float magnification;
        long baseLine;
        long start;
    }

    private static Singleton<Configuration> configurationSingleton = Singleton.with(
            new Supplier<Configuration>() {
                @Override
                public Configuration get() {
                    Socket socket = new Socket();
                    try {
                        socket.connect(new InetSocketAddress(getHost(), getPort()));
                        try {
                            byte[] buf = new byte[8 + 8 + 4];
                            int offset = 0;

                            do {
                                int count = socket.getInputStream().read(buf, offset, buf.length - offset);
                                if (count == -1) throw new RuntimeException("received error");
                                offset += count;
                            } while (offset < buf.length);
                            Configuration configuration = new Configuration();
                            ByteBuffer buffer = ByteBuffer.wrap(buf);
                            configuration.start = buffer.getLong(0);
                            configuration.baseLine = buffer.getLong(8);
                            configuration.magnification = buffer.getFloat(16);
                            return configuration;
                        } finally {
                            socket.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e.getLocalizedMessage(), e);
                    }
                }

                private String getHost() {
                    String host = Config.get(KEY_REMOTE_HOST, "clock");
                    return host == null ? System.getProperty(KEY_REMOTE_HOST) : host;
                }

                private int getPort() {
                    String port = Config.get(KEY_REMOTE_PORT, "clock");
                    if (port != null)
                        return Common.toInt(port, PORT);
                    else
                        return Common.toInt(System.getProperty(KEY_REMOTE_PORT), PORT);
                }

            }
    );

    public RemoteClockAgent() {
        super(configurationSingleton.get().magnification,
                configurationSingleton.get().baseLine,
                configurationSingleton.get().start);
    }
}
