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

package org.coodex.concrete.common;


/**
 * Created by davidoff shen on 2016-12-13.
 */
public class AttachmentInfoErrorCodes extends AbstractErrorCodes {

    protected final static int ATTACHMENT_INFO_SERVICE_BASE = CONCRETE_CORE + 1000;

    public final static int HMAC_ERROR = ATTACHMENT_INFO_SERVICE_BASE + 1;
    public final static int VERIFY_FAILED = ATTACHMENT_INFO_SERVICE_BASE + 2;
    public final static int NO_WRITE_PRIVILEGE = ATTACHMENT_INFO_SERVICE_BASE + 3;
    public final static int NO_READ_PRIVILEGE = ATTACHMENT_INFO_SERVICE_BASE + 4;
    public final static int ATTACHMENT_NOT_EXISTS = ATTACHMENT_INFO_SERVICE_BASE + 5;

}
