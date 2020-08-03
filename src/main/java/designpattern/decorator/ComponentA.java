package designpattern.decorator;

/**
 * @author rafa gao
 */


public class ComponentA implements Component {


    @Override
    public void operation() {
        System.out.println("原始对象进行操作");
    }
}
