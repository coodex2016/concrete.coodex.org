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
* Tracker server group
* @author Happy Fish / YuQing
* @version Version 1.17
*/
public class TrackerGroup
{
	protected Integer lock;
	public int tracker_server_index;
	public InetSocketAddress[] tracker_servers;
	
/**
* Constructor
* @param tracker_servers tracker servers
*/
	public TrackerGroup(InetSocketAddress[] tracker_servers)
	{
		this.tracker_servers = tracker_servers;
		this.lock = new Integer(0);
		this.tracker_server_index = 0;
	}

/**
* return connected tracker server
* @return connected tracker server, null for fail
*/
	public TrackerServer getConnection(int serverIndex) throws IOException
	{
		Socket sock = new Socket();
		sock.setReuseAddress(true);
		sock.setSoTimeout(ClientGlobal.g_network_timeout);
		sock.connect(this.tracker_servers[serverIndex], ClientGlobal.g_connect_timeout);
		return new TrackerServer(sock, this.tracker_servers[serverIndex]);
	}
	
/**
* return connected tracker server
* @return connected tracker server, null for fail
*/
	public TrackerServer getConnection() throws IOException
	{
		int current_index;
		
		synchronized(this.lock)
		{
			this.tracker_server_index++;
			if (this.tracker_server_index >= this.tracker_servers.length)
			{
				this.tracker_server_index = 0;
			}
			
			current_index = this.tracker_server_index;
		}
		
		try
		{
			return this.getConnection(current_index);
	  }
	  catch(IOException ex)
	  {
	  	System.err.println("connect to server " + this.tracker_servers[current_index].getAddress().getHostAddress() + ":" + this.tracker_servers[current_index].getPort() + " fail");
	  	ex.printStackTrace(System.err);
	  }
	  
	  for (int i=0; i<this.tracker_servers.length; i++)
	  {
	  	if (i == current_index)
	  	{
	  		continue;
	  	}
	  	
			try
			{
				TrackerServer trackerServer = this.getConnection(i);
				
				synchronized(this.lock)
				{
					if (this.tracker_server_index == current_index)
					{
						this.tracker_server_index = i;
					}
				}
				
				return trackerServer;
		  }
		  catch(IOException ex)
		  {
		  	System.err.println("connect to server " + this.tracker_servers[i].getAddress().getHostAddress() + ":" + this.tracker_servers[i].getPort() + " fail");
		  	ex.printStackTrace(System.err);
		  }
	  }
	  
	  return null;
	}

	public Object clone()
	{
		InetSocketAddress[] trackerServers = new InetSocketAddress[this.tracker_servers.length];
		for (int i=0; i<trackerServers.length; i++)
		{
			trackerServers[i] = new InetSocketAddress(this.tracker_servers[i].getAddress().getHostAddress(), this.tracker_servers[i].getPort());
		}
		
		return new TrackerGroup(trackerServers);
	}
}
