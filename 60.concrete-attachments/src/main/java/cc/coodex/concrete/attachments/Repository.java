package cc.coodex.concrete.attachments;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * 附件仓库，只关注附件存取
 * Created by davidoff shen on 2016-12-13.
 */
public interface Repository {

    AttachmentEntityInfo put(InputStream content, AttachmentInfo metaInfo);

    void updateInfo(String attachmentId, AttachmentInfo metaInfo);

    void delete(String attachmentId);

    void delete(Set<String> attachmentIds);

    AttachmentEntityInfo get(String attachmentId);

    void writeTo(String attachmentId, OutputStream outputStream, int speedLimit);

}
