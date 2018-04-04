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

/**
* Storage Server Info
* @author Happy Fish / YuQing
* @version Version 1.11
*/
public class StorageServer extends TrackerServer
{
	protected int store_path_index = 0;
	
/**
* Constructor
* @param ip_addr the ip address of storage server
* @param port the port of storage server
* @param store_path the store path index on the storage server
*/
	public StorageServer(String ip_addr, int port, int store_path) throws IOException
	{		
		super(ClientGlobal.getSocket(ip_addr, port), new InetSocketAddress(ip_addr, port));
		this.store_path_index = store_path;
	}

/**
* Constructor
* @param ip_addr the ip address of storage server
* @param port the port of storage server
* @param store_path the store path index on the storage server
*/
	public StorageServer(String ip_addr, int port, byte store_path) throws IOException
	{
		super(ClientGlobal.getSocket(ip_addr, port), new InetSocketAddress(ip_addr, port));
		if (store_path < 0)
		{
			this.store_path_index = 256 + store_path;
		}
		else
		{
			this.store_path_index = store_path;
		}
	}
	
/**
* @return the store path index on the storage server
*/
	public int getStorePathIndex()
	{
		return this.store_path_index;
	}
}
