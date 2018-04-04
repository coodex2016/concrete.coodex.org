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

/**
* Download file callback interface
* @author Happy Fish / YuQing
* @version Version 1.4
*/
public interface DownloadCallback
{
	/**
	* recv file content callback function, may be called more than once when the file downloaded
	* @param file_size file size
	*	@param data data buff
	* @param bytes data bytes
	* @return 0 success, return none zero(errno) if fail
	*/
	public int recv(long file_size, byte[] data, int bytes);
}
