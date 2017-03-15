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
