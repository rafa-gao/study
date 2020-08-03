package designpattern.chain;

/**
 * @author rafa gao
 */


public class HandlerA extends Handler{


    @Override
    protected void doHandler(Object obj) {
        if (obj != null) {
            System.out.println("obj被HandlerA处理了");
        }
    }
}
