package cc.coodex.concrete.attachments;

/**
 * 附件访问控制，负责验证附件读写授权
 * Created by davidoff shen on 2016-12-13.
 */
public interface AccessControl {

    boolean readable(String clientId, String tokenId, String attachmentId);

    boolean writable(String clientId, String tokenId);

    boolean deletable(String clientId, String tokenId, String attachmentId);
}
