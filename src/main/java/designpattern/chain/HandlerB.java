package designpattern.chain;

/**
 * @author rafa gao
 */


public class HandlerB extends Handler{


    @Override
    protected void doHandler(Object obj) {
        if (obj != null) {
            System.out.println("obj被HandlerB处理了");
        }
    }
}
