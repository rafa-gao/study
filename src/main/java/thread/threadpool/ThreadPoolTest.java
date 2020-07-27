package thread.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author rafa gao
 */
public class ThreadPoolTest {

    public static void main(String[] args) {
        changeCoreThreadPoolTest();
    }

    /**
     * 改变核心线程池
     * 在实验中我们可以了解到，当更新后的核心线程数大于目前正在工作的线程数时，会给每一个线程设置中断状态
     * 这样正在运行的线程仍然可以继续工作，但是当结束任务再次去获取线程的时候，挨个去唤醒当前的线程
     * @see ThreadPoolExecutor#getTask() 如果是在无限期阻塞获取任务时，线程会被唤醒，抛出异常
     * 当下一次获取任务时，会判断出线程过多，然后采取超时获取的策略，以便于平滑的处理核心线程的数量
     */
    public static void changeCoreThreadPoolTest() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3,
                4,
                1,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10));

        threadPoolExecutor.execute(new Task());
        threadPoolExecutor.execute(new Task());
        threadPoolExecutor.execute(new Task());
        threadPoolExecutor.execute(new Task());
        threadPoolExecutor.execute(new Task());
        threadPoolExecutor.setCorePoolSize(2);
    }

    private static class Task implements Runnable {

        @Override
        public void run() {
            System.out.println("我被执行了");
        }
    }

}
