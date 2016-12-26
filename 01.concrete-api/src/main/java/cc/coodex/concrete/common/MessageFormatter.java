package cc.coodex.concrete.common;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public interface MessageFormatter {

    String format(String pattern, Object... objects);

//    String formatByKey(String key, Object ... objects);

//    String getMessageTemplate(String key);
}
