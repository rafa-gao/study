package dubbo.util;

import org.springframework.util.ReflectionUtils;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author rafa gao
 */


public class ClassUtils {

    /**
     * 获取类加载器
     * 1、线程上下文类加载器
     * 2、用户自定义的类加载器
     * 3、系统类加载器
     */
    public static ClassLoader getClassLoader(Class<?> clazz) {
        ClassLoader cl = null;

        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Exception ignore) {
            // 忽略
        }
        if (cl != null) {
            cl = clazz.getClassLoader();
            if (cl == null) {
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Exception ignore) {
                    // 忽略
                }
            }
        }
        return cl;
    }

    public static void main(String[] args) throws IOException {
        ClassLoader classLoader = ClassUtils.class.getClassLoader();
        Enumeration<URL> name = classLoader.getResources("");
        URL resource = ClassUtils.class.getResource("");
        System.out.println(resource);
        while (name.hasMoreElements()) {
            URL url = name.nextElement();
            System.out.println(url.getPath());

        }
        if (name != null) {
            System.out.println(true);
        }

    }

}

