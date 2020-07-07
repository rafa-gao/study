package single;

/**
 * 借助final实现，一目了然，不必多说
 * 缺点：恶寒模式，会有一定的内存浪费
 * @author rafa gao
 */
public class SingleWithFinal {

    private static final SingleWithFinal singleWithFinal = new SingleWithFinal();


    private SingleWithFinal() {
    }

    public static SingleWithFinal getSingleWithFinal() {
        return singleWithFinal;
    }
}
