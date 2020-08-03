package designpattern.chain;

/**
 * 链路中每一个具体的处理对象
 *
 * @author rafa gao
 */
public abstract class Handler {

    protected Handler successor;

    public void handler(Object obj) {
        doHandler(obj);
        if (successor != null) {
            successor.handler(obj);
        }
    }

    protected abstract void doHandler(Object obj);

    public void setSuccessor(Handler successor) {
        this.successor = successor;
    }

}
