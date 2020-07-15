package classloader;

import java.lang.reflect.InvocationTargetException;

/**
 * @author rafa gao
 */
public class DriverManageTest {

    public static void main(String[] args) {
        myThreadTest();
    }

    private static void myThreadTest(){

        RunnableMy runnableMy = new RunnableMy();
        // un.misc.Launcher$AppClassLoader
        System.out.println(runnableMy.getClass().getClassLoader());
        // null(启动类加载器)
        System.out.println(Runnable.class.getClassLoader());
    }

    static class RunnableMy implements Runnable {
        @Override
        public void run() {
            return;
        }

    }

}
