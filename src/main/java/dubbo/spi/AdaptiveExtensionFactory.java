package dubbo.spi;

import dubbo.annotation.Adaptive;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rafa gao
 */

@Adaptive
public class AdaptiveExtensionFactory implements ExtensionFactory{

    // 相当于存储了所有的ExtensionFactory的实现类
    private final List<ExtensionFactory> factories;

    public AdaptiveExtensionFactory() {
        ExtensionLoader<ExtensionFactory> extensionLoader = ExtensionLoader.getExtensionLoader(ExtensionFactory.class);
        List<ExtensionFactory> factories = new ArrayList<>();

        for (String extensionName : extensionLoader.getSupportedExtensions()) {
            factories.add(extensionLoader.getExtension(extensionName));
        }
        this.factories = factories;
    }

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        T extension;
        for (ExtensionFactory factory : factories) {
            extension = factory.getExtension(type, name);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }
}
