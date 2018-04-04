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
import java.net.InetSocketAddress;
import java.net.Socket;

/**
* Server Info
* @author Happy Fish / YuQing
* @version Version 1.7
*/
public class ServerInfo
{
	protected String ip_addr;
	protected int port;
	
/**
* Constructor
* @param ip_addr address of the server
* @param port the port of the server
*/
	public ServerInfo(String ip_addr, int port)
	{
		this.ip_addr = ip_addr;
		this.port = port;
	}
	
/**
* return the ip address
* @return the ip address
*/
	public String getIpAddr()
	{
		return this.ip_addr;
	}
	
/**
* return the port of the server
* @return the port of the server
*/
	public int getPort()
	{
		return this.port;
	}
	
/**
* connect to server
* @return connected Socket object
*/
	public Socket connect() throws IOException
	{
		Socket sock = new Socket();
		sock.setReuseAddress(true);
		sock.setSoTimeout(ClientGlobal.g_network_timeout);
		sock.connect(new InetSocketAddress(this.ip_addr, this.port), ClientGlobal.g_connect_timeout);
		return sock;
	}
}
