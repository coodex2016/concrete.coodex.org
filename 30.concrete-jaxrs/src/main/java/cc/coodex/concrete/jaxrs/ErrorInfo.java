package cc.coodex.concrete.jaxrs;

import cc.coodex.concrete.common.AbstractErrorCodes;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class ErrorInfo {
    private int code = AbstractErrorCodes.OK;
    private String msg = "";

    public ErrorInfo() {
    }

    public ErrorInfo(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
