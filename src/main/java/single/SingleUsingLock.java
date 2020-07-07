package single;

/**
 * 通过加锁来实现
 * 优势：实现比较简单
 * 劣势：每次方法都需要加锁，加锁只是在第一次发生冲突的时候起作用，在后面多次获取对象的时候加锁都会造成并发上的障碍
 * @author rafa gao
 */
public class SingleUsingLock {

    private static SingleUsingLock singleUsingLock;

    private SingleUsingLock() {
    }

    public static SingleUsingLock getSingleUsingLock() {
        synchronized (SingleUsingLock.class) {
            if (singleUsingLock == null) {
                SingleUsingLock single = new SingleUsingLock();
                singleUsingLock = single;
                return single;
            }
        }
        return singleUsingLock;
    }
}
