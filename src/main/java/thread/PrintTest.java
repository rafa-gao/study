package thread;

/**
 * 多线程交替打印
 *
 * @author rafa gao
 */
public class PrintTest {


    public static void main(String[] args) {

        Thread threadA = new Thread(new RunnableA());
        Thread threadB = new Thread(new RunnableB());
        threadA.start();
        threadB.start();
    }

    private static class RunnableA implements Runnable{
        @Override
        public void run() {
            int count = 1;
            while (true) {
                synchronized (PrintTest.class) {
                    PrintTest.class.notifyAll();
                    System.out.println(count);
                    count++;
                    if (count > 5) {
                        return;
                    }
                    try {
                        PrintTest.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private static class RunnableB implements Runnable{
        @Override
        public void run() {
            int count = 97;
            while (true) {
                synchronized (PrintTest.class) {
                    PrintTest.class.notifyAll();
                    System.out.println((char) count);
                    count++;
                    if (count>102) {
                        return;
                    }
                    try {
                        PrintTest.class.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
