package org.coodex.concrete.apitools;

import java.io.IOException;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public interface ConcreteAPIRender {

    /**
     * <pre>例如：
     *   服务提供类型：JaxRS
     *   类型：code, doc
     *   使用者：backend, jquery, angularjs, angualr2, java, c#等
     *   文档化格式：gitbook, asciidoctor, markdown</pre>
     *
     * @param desc <i>服务提供类型</i>.<i>类型</i>.<i>使用者</i>.<i>文档化格式</i>.<i>版本</i>
     * @return
     */
    boolean isAccept(String desc);

    /**
     * @param packages 检索的包
     */
    void writeTo(String ... packages) throws IOException;


    void setRoot(String rootPath);

}
