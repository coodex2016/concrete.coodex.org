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
import java.lang.reflect.Array;

/**
* C struct body decoder
* @author Happy Fish / YuQing
* @version Version 1.17
*/
public class ProtoStructDecoder<T extends StructBase>
{	
/**
* Constructor
*/
	public ProtoStructDecoder()
	{
	}
	
/**
* decode byte buffer
*/
@SuppressWarnings("unchecked")
	public T[] decode(byte[] bs, Class<T> clazz, int fieldsTotalSize) throws Exception
	{
		if (bs.length % fieldsTotalSize != 0)
		{
			throw new IOException("byte array length: " + bs.length + " is invalid!");
		}
		
		int count = bs.length / fieldsTotalSize;
		int offset;
		T[] results = (T[])Array.newInstance(clazz, count);
		
		offset = 0;
		for (int i=0; i<results.length; i++)
		{
			results[i] = clazz.newInstance();
			results[i].setFields(bs, offset);
			offset += fieldsTotalSize;
		}
		
		return results;
	}
}
