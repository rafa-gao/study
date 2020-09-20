package dubbo.spi;

import dubbo.annotation.SPI;

/**
 * @author rafa gao
 */

@SPI
public interface ExtensionFactory {


    <T> T getExtension(Class<T> type, String name);
}
