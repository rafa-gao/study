package spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;

import java.util.Map;
import java.util.Properties;

/**
 * Spring 容器的顶层接口是 {@link org.springframework.beans.factory.BeanFactory}
 * @see org.springframework.context.ApplicationContext 是一个高级容器
 *
 * Spring 中对于对象的定义是通过{@link org.springframework.beans.factory.config.BeanDefinition}描述的
 *
 * @author rafa gao
 */
@SuppressWarnings("ALL")
public class SpringIOCTest {


    /**
     * 这个类是一个最终的大Boss
     * 首先关注它的初始化，构建了两个关键的对象
     * @see AnnotatedBeanDefinitionReader 从名称我们可以知道，这个类是用来解析BeanDifinition的
     * @see AnnotatedBeanDefinitionReader#getOrCreateEnvironment(BeanDefinitionRegistry) 这个方法首先回去加载出一个环境
     *
     */
    public static void annotationConfigServletWebServerApplicationContext() {


    }

    public static void main(String[] args) {
        // 获取Java相关的而一些属性值
        Properties properties = System.getProperties();
        // 获取环境变量
        Map<String, String> env = System.getenv();
    }

}
