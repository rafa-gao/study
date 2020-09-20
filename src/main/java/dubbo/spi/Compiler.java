package dubbo.spi;

/**
 * @author rafa gao
 */


public interface Compiler {

    Class<?> compile(String code, ClassLoader classLoader);

}
