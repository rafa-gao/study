package thread;

/**
 * @author rafa gao
 */


public class ThreadUtils {

    public static void join(Thread thread) throws InterruptedException {
        synchronized (thread) {
            while (thread.isAlive()) {
                thread.wait();
            }
        }
    }
}
