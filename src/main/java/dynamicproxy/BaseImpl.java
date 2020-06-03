package dynamicproxy;

/**
 * @author rafa gao
 */
public class BaseImpl implements Base {

    @Override
    public void methodA() {
        System.out.println("原本的methodA方法被调用");
    }

//    @Override
//    public void methodB() {
//        return;
//    }
}
