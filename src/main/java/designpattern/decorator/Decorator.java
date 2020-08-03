package designpattern.decorator;

/**
 * 增加的具体功能
 *
 * @author rafa gao
 */
public  class Decorator implements Component {


    // 需要被装饰的对象
    private Component component;

    public Decorator(Component component) {
        super();
        this.component = component;
    }

    /**
     * 首先委托给原对象
     */
    @Override
    public void operation() {
        component.operation();
    }
}
