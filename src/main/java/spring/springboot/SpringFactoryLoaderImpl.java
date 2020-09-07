package spring.springboot;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * 加载SpringFactories
 * @author rafa gao
 */


public class SpringFactoryLoaderImpl {

    private static final String FACTORIES_RESOURCES_LOCATION = "META-INFO/spring.factories";

    private static final Map<ClassLoader, MultiValueMap<String, String>> cache = new ConcurrentReferenceHashMap<>();

    public static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
        MultiValueMap<String, String> result = cache.get(classLoader);
        if (result != null) {
            return result;
        }

        try {
            Enumeration<URL> urls = classLoader == null ? classLoader.getResources(FACTORIES_RESOURCES_LOCATION) : ClassLoader.getSystemResources(FACTORIES_RESOURCES_LOCATION);
            result = new LinkedMultiValueMap<>();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                UrlResource urlResource = new UrlResource(url);
                Properties properties = PropertiesLoaderUtils.loadProperties(urlResource);
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    String key = (String) entry.getKey();
                    String[] values = StringUtils.commaDelimitedListToStringArray((String) entry.getValue());
                    for (String value : values) {
                        result.add(key,value.trim());
                    }
                }
                cache.put(classLoader, result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
