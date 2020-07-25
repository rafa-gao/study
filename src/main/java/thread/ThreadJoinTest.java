package thread;

/**
 * @author rafa gao
 */


public class ThreadJoinTest {

    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("等待三秒结束");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.start();
        try {
            ThreadUtils.join(thread1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("thread1 run end");
    }
}
