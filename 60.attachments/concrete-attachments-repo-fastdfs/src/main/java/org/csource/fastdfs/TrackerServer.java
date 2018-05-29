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

package org.csource.fastdfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Tracker Server Info
 *
 * @author Happy Fish / YuQing
 * @version Version 1.11
 */
public class TrackerServer {
    protected Socket sock;
    protected InetSocketAddress inetSockAddr;

    /**
     * Constructor
     *
     * @param sock         Socket of server
     * @param inetSockAddr the server info
     */
    public TrackerServer(Socket sock, InetSocketAddress inetSockAddr) {
        this.sock = sock;
        this.inetSockAddr = inetSockAddr;
    }

    /**
     * get the connected socket
     *
     * @return the socket
     */
    public Socket getSocket() throws IOException {
        if (this.sock == null) {
            this.sock = ClientGlobal.getSocket(this.inetSockAddr);
        }

        return this.sock;
    }

    /**
     * get the server info
     *
     * @return the server info
     */
    public InetSocketAddress getInetSocketAddress() {
        return this.inetSockAddr;
    }

    public OutputStream getOutputStream() throws IOException {
        return this.sock.getOutputStream();
    }

    public InputStream getInputStream() throws IOException {
        return this.sock.getInputStream();
    }

    public void close() throws IOException {
        if (this.sock != null) {
            try {
                ProtoCommon.closeSocket(this.sock);
            } finally {
                this.sock = null;
            }
        }
    }

    protected void finalize() throws Throwable {
        this.close();
    }
}
