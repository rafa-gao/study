package designpattern.decorator;

/**
 * @author rafa gao
 */


public class ConcreteDecoratorB extends Decorator{


    public ConcreteDecoratorB(Component component) {
        super(component);
    }

    @Override
    public void operation() {
        super.operation();
        System.out.println("装饰器B起作用了");
    }
}
