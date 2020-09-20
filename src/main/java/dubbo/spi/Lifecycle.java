package dubbo.spi;

/**
 * @author rafa gao
 */


public interface Lifecycle {

    void initialize() throws IllegalStateException;
}
