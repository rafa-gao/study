package designpattern.chain;

/**
 * @author rafa gao
 */


public class ChainTest {

    public static void main(String[] args) {
        HandlerA handlerA = new HandlerA();
        HandlerB handlerB = new HandlerB();
        handlerA.setSuccessor(handlerB);

        handlerA.handler(new Object());
    }
}
