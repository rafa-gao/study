package spring;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author rafa gao
 */
public class AbstractAutowireCapableBeanFactoryImpl extends AbstractAutowireCapableBeanFactory {

    // 存放未完成的 BeanWrapper对象
    private final ConcurrentMap<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    // 是否主动的解决循环依赖的问题
    private boolean allowCircularReferences = true;



    /**
     * 创建出Bean
     *
     * 流程：
     * 获取BeanWrapper包装类（缓存或者新建）->拿到bean对象 -> 采用处理器处理RootBeanDefinition
     * -> 提前暴露Bean,解决循环依赖-> 实例化对象
     *
     * @param beanName bean名称
     * @param mbd 关于bean定义的一些元数据
     * @param args 构造函数使用的参数
     * @return bean的新实例
     * @throws BeanCreationException
     */
    public Object doGetBean(final String beanName, final RootBeanDefinition mbd,final @NonNull Object[] args) throws BeanCreationException {
        BeanWrapper instanceBeanWrapper = null;
        // 处理单例对象
        if (mbd.isSingleton()) {
            instanceBeanWrapper = this.factoryBeanInstanceCache.remove(beanName);
        }
        // 缓存中没有，直接创建
        if (instanceBeanWrapper == null) {
            instanceBeanWrapper = createBeanInstance(beanName, mbd, args);
        }
        // 获取包装对象里的被包装对象
        final Object bean = instanceBeanWrapper.getWrappedInstance();
        // 获取对象的class
        Class<?> beanType = instanceBeanWrapper.getWrappedClass();
        // 设置RootBeanDefinition的目标类
        if (beanType != null) {
            mbd.setTargetType(beanType);
        }
        /**
         * 采用 post-processor 处理bean的定义
         * 首先需要获取 {@link RootBeanDefinition#postProcessingLock} 这把锁
         * 由于这里获取过于麻烦，所以采取锁住整个对象来简化流程
         */
        //
        // 首先需要获取
        synchronized (mbd) {
            /**
             * 这里相当于一个双重检查，判断是否已经被处理器处理过了
             * 同样拿不到 呜呜呜！！！
             * @see RootBeanDefinition#postProcessed
             */
            if ("postProcessed" != null) {
                for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
                    if (beanPostProcessor instanceof MergedBeanDefinitionPostProcessor) {
                        ((MergedBeanDefinitionPostProcessor) beanPostProcessor).postProcessMergedBeanDefinition(mbd, beanType, beanName);
                    }
                }
                // 上面那个参数设为true
            }
        }

        // 重中之重！！！！！=======================解决循环依赖

        /**
         * 三个条件决定了是否需要进行提前暴露
         * 1、单例
         * 2、正在创建中
         * 3、该Factory具有解决循环依赖的能力
         */
        boolean earlySingletonExposure = mbd.isSingleton() && this.allowCircularReferences && isSingletonCurrentlyInCreation(beanName);
        if (earlySingletonExposure) {
            System.out.println(beanName + "被提前暴露了");
            // 这里没有直接的放一个 Bean，而是放入了一个 ObjectFactory
            addSingletonFactory(beanName,()->{
                Object exposedBean = bean;
                if (mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
                    for (BeanPostProcessor processor : getBeanPostProcessors()) {
                        if (processor instanceof SmartInstantiationAwareBeanPostProcessor) {
                            exposedBean = ((SmartInstantiationAwareBeanPostProcessor) processor).getEarlyBeanReference(exposedBean, beanName);
                        }
                    }
                }
                return exposedBean;
            });
        }

        // 实例化对象
        Object exposedObject = bean;
        try {
            // 填充对象
            populateBean(beanName, mbd, instanceBeanWrapper);
            // 实例化对象
            exposedObject = initializeBean(exposedObject, beanName);
        } catch (Exception ex) {
            if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
                throw (BeanCreationException) ex;
            }
            else {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
            }
        }

        // 如果采取了早期暴露
        if (earlySingletonExposure) {

        }

        return exposedObject;

    }



    @Override
    public <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException {
        return null;
    }

    @Override
    public Object resolveDependency(DependencyDescriptor descriptor, String requestingBeanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {
        return null;
    }

    @Override
    protected boolean containsBeanDefinition(String beanName) {
        return false;
    }

    @Override
    protected BeanDefinition getBeanDefinition(String beanName) throws BeansException {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return null;
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
        return null;
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
        return null;
    }
}
