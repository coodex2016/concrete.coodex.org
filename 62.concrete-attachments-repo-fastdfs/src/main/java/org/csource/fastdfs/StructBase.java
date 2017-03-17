/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.csource.fastdfs;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
* C struct body decoder
* @author Happy Fish / YuQing
* @version Version 1.17
*/
public abstract class StructBase
{
	protected static class FieldInfo
	{
		protected String name;
		protected int offset;
		protected int size;
		
		public FieldInfo(String name, int offset, int size)
		{
			this.name = name;
			this.offset = offset;
			this.size = size;
		}
	}
	
/**
* set fields
* @param bs byte array
* @param offset start offset
*/
	public abstract void setFields(byte[] bs, int offset);
	
	protected String stringValue(byte[] bs, int offset, FieldInfo filedInfo)
	{
		try
		{
			return (new String(bs, offset + filedInfo.offset, filedInfo.size, ClientGlobal.g_charset)).trim();
		}
		catch(UnsupportedEncodingException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	protected long longValue(byte[] bs, int offset, FieldInfo filedInfo)
	{
		return ProtoCommon.buff2long(bs, offset + filedInfo.offset);
	}
	
	protected int intValue(byte[] bs, int offset, FieldInfo filedInfo)
	{
		return (int)ProtoCommon.buff2long(bs, offset + filedInfo.offset);
	}
	
	protected int int32Value(byte[] bs, int offset, FieldInfo filedInfo)
	{
		return ProtoCommon.buff2int(bs, offset + filedInfo.offset);
	}

	protected byte byteValue(byte[] bs, int offset, FieldInfo filedInfo)
	{
		return bs[offset + filedInfo.offset];
	}

	protected boolean booleanValue(byte[] bs, int offset, FieldInfo filedInfo)
	{
		return bs[offset + filedInfo.offset] != 0;
	}
	
	protected Date dateValue(byte[] bs, int offset, FieldInfo filedInfo)
	{
		return new Date(ProtoCommon.buff2long(bs, offset + filedInfo.offset) * 1000);
	}
}
