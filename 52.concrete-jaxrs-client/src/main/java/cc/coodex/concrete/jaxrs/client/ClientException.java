package cc.coodex.concrete.jaxrs.client;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class ClientException extends RuntimeException {
    private int code;
    private String path;
    private String method;

    public ClientException(int code, String msg, String path, String method) {
        super(msg);
        this.code = code;
        this.path = path;
        this.method = method;
    }

    public int getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }
}
