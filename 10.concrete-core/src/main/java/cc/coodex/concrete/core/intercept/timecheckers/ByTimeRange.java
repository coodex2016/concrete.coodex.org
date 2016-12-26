package cc.coodex.concrete.core.intercept.timecheckers;

import cc.coodex.concrete.api.ServiceTimingChecker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * <pre>按照时间段提供服务
 * 属性：
 * range: HH:mm-HH:mm;...
 * </pre>
 * <p>
 * Created by davidoff shen on 2016-11-02.
 */
public class ByTimeRange implements ServiceTimingChecker {

    private static final DateFormat format = new SimpleDateFormat("HH:mm");

    private String range;


    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    @Override
    public boolean isAllowed() {
        if (range == null) return true;
        String now = format.format(new Date());
        StringTokenizer st = new StringTokenizer(range, ";");
        while (st.hasMoreElements()) {
            String str = st.nextToken().trim();
            int index = str.indexOf('-');
            if (index > 0) {
                String start = str.substring(0, index);
                String end = str.substring(index + 1);
                if (start.compareTo(now) <= 0 && end.compareTo(now) >= 0)
                    return true;
            }
        }

        return false;
    }


}
