package designpattern.decorator;

/**
 * @author rafa gao
 */


public class ConcreteDecoratorA extends Decorator{


    public ConcreteDecoratorA(Component component) {
        super(component);
    }

    @Override
    public void operation() {
        super.operation();
        System.out.println("装饰器A起作用了");
    }
}
