package designpattern.decorator;

/**
 * @author rafa gao
 */


public class UseTest {

    public static void main(String[] args) {
        Component componentA = new ComponentA();
        Component concreteDecoratorA = new ConcreteDecoratorA(componentA);
        Component concreteDecoratorB = new ConcreteDecoratorB(componentA);

        componentA.operation();
        System.out.println("===========");
        concreteDecoratorA.operation();
        System.out.println("===========");
        concreteDecoratorB.operation();

    }
}
