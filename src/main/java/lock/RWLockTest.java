package lock;

import java.util.concurrent.locks.Lock;

/**
 * 自制读写锁测试
 * @author rafa gao
 */
public class RWLockTest {


    public static void main(String[] args) {
        RWLock rwLock = new RWLock();
        Lock readLock = rwLock.readLock();
        Lock writeLock = rwLock.writeLock();
    }

}
