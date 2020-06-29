package lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * 自制读写锁
 *
 * @author rafa gao
 */
public class RWLock implements ReadWriteLock {


    private final RLock readLock;

    private final WLock writeLock;

    private Sync sync;

    public RWLock() {
        readLock = new RLock(this);
        writeLock = new WLock(this);
    }


    /**
     * 核心部分(抽象队列同步器)
     * <p>
     * 低16位保存写锁的状态，高16位保存读锁的状态
     */
    private static class Sync extends AbstractQueuedSynchronizer {

        Sync(){
            readHolds = new ThreadLocalHoldCounter();
            // 确保readHolds的可见性
            setState(getState());
        }



        static final int HALF = 16;
        // 读、写锁各自的最大数量
        static final int MAX_COUNT = (1 << HALF) - 1;
        static final int SHARED_UNIT = (1 << HALF);
        static final int EXCLUSIVE_MASK = (1 << HALF) - 1;

        // 获取独占锁的数量
        static int getExclusiveCount(int state) {
            return state & EXCLUSIVE_MASK;
        }

        // 获取共享锁的数量
        static int sharedCount(int c) {
            return c >>> HALF;
        }

        /**
         * 第一个读线程
         */
        private Thread firstReader = null;
        private int firstReaderHoldCount;

        private static class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
            @Override
            protected HoldCounter initialValue() {
                return new HoldCounter();
            }
        }

        /**
         * 线程安全的本地变量
         */
        private transient ThreadLocalHoldCounter readHolds;

        /**
         * 本地缓存的HoldCounter，保存最后一个获取读锁的HoldCounter
         */
        private transient HoldCounter cachedHoldCounter;

        static final class HoldCounter {
            int count = 0;
            // 保存线程的id，防止内存泄漏
            long threadId = getThreadId(Thread.currentThread());
        }

        /**
         * 该方法是读锁使用
         *
         * @param arg
         * @return
         */
        @Override
        protected boolean tryAcquire(int arg) {
            // 获取当前线程
            Thread thread = Thread.currentThread();
            int state = getState();
            int w = getExclusiveCount(state);
            // 存在读锁or写锁
            if (state != 0) {
                // 存在不是当前线程的读锁
                if (w == 0 || getExclusiveOwnerThread() != thread) {
                    return false;
                }
                setState(arg + state);
                return true;
            }
            // 不存在任何锁
            if (readerShouldBlock() && !compareAndSetState(state , state + arg)) {
                return false;
            }
            setExclusiveOwnerThread(thread);
            return true;
        }

        @Override
        protected boolean tryRelease(int arg) {
            return super.tryRelease(arg);
        }

        /**
         * 实现获取共享锁的过程
         *
         * @param arg
         * @return
         */
        @Override
        protected int tryAcquireShared(int arg) {
            Thread t = Thread.currentThread();
            // 获取当前读写锁的状态
            int c = getState();
            // 存在写锁&&拥有写锁的线程不是当前线程，获取锁失败
            // 写锁不存在，就可以进行读锁的获取了
            if (getExclusiveCount(c) != 0 && getExclusiveOwnerThread() != t) {
                return -1;
            }
            int r = sharedCount(c);
            // 没有任何的共享锁&&进行读锁+1成功
            if (r < MAX_COUNT && compareAndSetState(c, c + SHARED_UNIT)) {
                // 第一次获取读锁
                if (r == 0) {
                    firstReader = t;
                    firstReaderHoldCount = 1;
                } else if (t == firstReader) {
                    firstReaderHoldCount++;
                } else {
                    HoldCounter hc = this.cachedHoldCounter;
                    // 有另外一个线程进行写操作，利用本地变量进行保存，同时还有一个公用的做缓存
                    if (hc == null || hc.threadId != getThreadId(t)) {
                        hc = this.cachedHoldCounter = readHolds.get();
                    } else if (hc.count == 0) {
                        readHolds.set(hc);
                    }
                    hc.count++;
                }
                return 1;
            }
            return fullTryAcquireShared(t);
        }

        private int fullTryAcquireShared(Thread t) {
            // TODO
            return -1;
        }

        /**
         * 实现了非公平锁
         */
        final boolean readerShouldBlock() {
            // TODO
            return false;
        }


        @Override
        protected boolean tryReleaseShared(int arg) {
            return super.tryReleaseShared(arg);
        }
    }


    /**
     * 读锁
     */
    public static class RLock implements Lock {

        private Sync sync;

        public RLock(RWLock rwLock) {
            this.sync = rwLock.sync;
        }

        /**
         * 读锁直接采用tryAcquire
         */
        @Override
        public void lock() {
            // 使用AQS，获取共享锁
            sync.acquireShared(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {

        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unlock() {

        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }

    public static class WLock implements Lock {

        private Sync sync;

        public WLock(RWLock rwLock) {
            this.sync = rwLock.sync;
        }

        @Override
        public void lock() {
            sync.acquire(1);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {

        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unlock() {

        }

        @Override
        public Condition newCondition() {
            return null;
        }
    }


    @Override
    public Lock readLock() {
        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }

    private static long getThreadId(Thread currentThread) {
        return currentThread.getId();
    }

}
