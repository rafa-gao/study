package single;

/**
 * 通过内部内来实现了懒加载
 * @author rafa gao
 */


public class SingleWithFinalAndInner {

    private SingleWithFinalAndInner() {
    }

    private static class SingleHolder{
        private static final SingleWithFinalAndInner INSTANCE=new SingleWithFinalAndInner();
    }

    public static SingleWithFinalAndInner getSingle() {
        return SingleHolder.INSTANCE;
    }
}
