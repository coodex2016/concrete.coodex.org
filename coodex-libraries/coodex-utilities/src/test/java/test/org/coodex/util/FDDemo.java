package test.org.coodex.util;

import org.coodex.concurrent.Debounce;
import org.coodex.concurrent.Throttle;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class FDDemo {

    public static class CountableRunnable implements Runnable{
        private AtomicInteger count = new AtomicInteger(0);
        private final String info;

        public CountableRunnable(String info) {
            this.info = info;
        }

        @Override
        public void run() {
            log.info(info);
            count.incrementAndGet();
        }

        public int getCount(){
            return count.get();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(FDDemo.class);

    public static void main(String[] args) {
        CountableRunnable debounceRunnable = new CountableRunnable("debounce executed.");
        Debounce debounce = Debounce.newBuilder()
                .idle(15)// 空闲15毫秒才执行
                .runnable(debounceRunnable)
                .build();
        for (int i = 0; i < 1000; i++) {
            debounce.submit(
                    // debounceRunnable // 另一种方式
            );
            Common.sleep(Common.random(15));
        }
        Common.sleep(15);
        log.info("debounce: executed {} times.", debounceRunnable.getCount());

        CountableRunnable throttleRunnable = new CountableRunnable("biu~");

        Throttle throttle = Throttle.newBuilder()
                .interval(500)
                .runnable(throttleRunnable)
                .build();

        for(int i = 0; i < 1000; i ++){
            throttle.submit(
                    // throttleRunnable
            );
            Common.sleep(Common.random(15));
        }
        Common.sleep(500);
        log.info("throttle: executed {} times.", throttleRunnable.getCount());
    }

}
