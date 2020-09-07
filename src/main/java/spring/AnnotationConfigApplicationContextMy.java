package spring;

import com.sun.xml.internal.bind.v2.model.annotation.AnnotationReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

/**
 * 以注解为基础的 IOC 容器源码分析
 * @author rafa gao
 */
public class AnnotationConfigApplicationContextMy extends AnnotationConfigApplicationContext {

    private AnnotatedBeanDefinitionReader reader;

    private ClassPathBeanDefinitionScanner scanner;

    public AnnotationConfigApplicationContextMy() {
        reader = new AnnotatedBeanDefinitionReader(this);
        scanner = new ClassPathBeanDefinitionScanner(this);
    }

    /**
     *
     * @param annotatedClasses 对应的配置类
     */
    public AnnotationConfigApplicationContextMy(Class<?>... annotatedClasses) {
        super(annotatedClasses);
        register(annotatedClasses);
        refresh();
    }
}
