package dubbo.spi;

/**
 * @author rafa gao
 */


public interface LoadingStrategy {

    String directory();
    default boolean preferExtensionClassLoader() {
        return false;
    }

    default String excludedPackages(){
        return null;
    }


}
