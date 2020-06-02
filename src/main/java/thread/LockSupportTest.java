package thread;

import java.util.concurrent.locks.LockSupport;

/**
 * @author rafa gao
 */


public class LockSupportTest {

    /**
     * 测试调用{@link LockSupport#parkNanos(Object, long)}}后，线程的状态
     * 结论：线程进入TIMED_WAITING状态
     */
    public static void conditionAfterPark() {

        Thread lockSupportThread = new Thread("LockSupportThread"){
            @Override
            public void run() {
                long start = System.nanoTime();
                LockSupport.parkNanos(this,5000000000L);
                long end = System.nanoTime();
                System.out.println(String.format("park总时长：%s",(end-start)));
            }
        };
        lockSupportThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread.State state = lockSupportThread.getState();
        System.out.println("调用超时park后，线程状态是："+state.name());
    }

//    public static void getLockSupportThread() {
//
//    }

    public static void main(String[] args) {
        conditionAfterPark();
    }
}
