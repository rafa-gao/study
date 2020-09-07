package spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.core.env.Environment;

/**
 * @author rafa gao
 */
public class AnnotatedBeanDefinitionReaderImpl extends AnnotatedBeanDefinitionReader {



    public AnnotatedBeanDefinitionReaderImpl(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public AnnotatedBeanDefinitionReaderImpl(BeanDefinitionRegistry registry, Environment environment) {
        super(registry, environment);
    }
}
