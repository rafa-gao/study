package single;

/**
 * 使用volatile来修饰对象的目的
 * 1、保证了对象的可见性
 * 2、确保了创建对象时不会被指令重排序
 * 创建对象的完整过程：1、为对象分配内存空间 2、初始化对象 3、将指针指向分配在堆中的对象
 * 但是由于指令重排序的存在，2、3两步在实际的执行顺序上可能会颠倒，使用volatile可以禁止指令重排序，从而可以正确的初始化对象
 *
 * @author rafa gao
 */
public class SingleWithVolatile {

    private static volatile SingleWithVolatile singleWithVolatile;

    private SingleWithVolatile() {

    }

    public static SingleWithVolatile getSingleWithVolatile() {
        if (singleWithVolatile == null) {
            synchronized (SingleWithVolatile.class) {
                if (singleWithVolatile == null) {
                    singleWithVolatile = new SingleWithVolatile();
                }
            }
        }
        return singleWithVolatile;
    }

}
