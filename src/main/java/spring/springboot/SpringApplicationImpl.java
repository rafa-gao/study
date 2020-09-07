package spring.springboot;

import com.sun.xml.internal.ws.api.addressing.WSEndpointReference;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.*;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.SimpleCommandLineArgsParser;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * 主要用来分析Spring的启动流程
 *
 * @author rafa gao
 */
public class SpringApplicationImpl extends SpringApplication {


    private static final String SYSTEM_PROPERTY_JAVA_AWT_HEADLESS = "headless";
    private boolean headless = true;

    private ConfigurableEnvironment environment;

    private boolean isCustomEnvironment;




    /**
     * 设置资源加载器->推断应用类型->设置初始化器->设置监听者
     * @param resourceLoader
     * @param primarySources
     */
    public SpringApplicationImpl(ResourceLoader resourceLoader, Class<?>... primarySources) {
        // 设置资源加载器
        setResourceLoader(resourceLoader);
        addPrimarySources(Arrays.asList(primarySources));
        /**
         * @see WebApplicationType#deduceFromApplicationContext(Class)
         * 主要是通过Class#forname去加载class 如果能够加载对应的class，就能推断出类型
         */
        setWebApplicationType(WebApplicationType.SERVLET);
        // 设置初始化器
        setInitializers(getSpringFactoriesInstances(ApplicationContextInitializer.class, new Class<?>[]{}, null));
        setListeners(getSpringFactoriesInstances(ApplicationListener.class, new Class<?>[]{}, null));
        /**
         * 设置主函数所在的类
         * @see SpringApplication#deduceMainApplicationClass()
         */
        setMainApplicationClass(null);
    }


    @Override
    public ConfigurableApplicationContext run(String... args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        configureHeadlessProperty();
        SpringApplicationRunListeners runListener = getRunListener(args);
        // 开始监听，这里暂时不去深究
        runListener.starting();
        ConfigurableApplicationContext context = null;
        Collection<SpringBootExceptionReporter> exceptionReporters;
        try {
            // 构造启动参数
            DefaultApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            // 准备环境
            prepareEnvironment(applicationArguments, runListener);
            /**
             * 配置忽略的BeanInfo
             * 打印环境变量
             * @see SpringApplication#configureIgnoreBeanInfo(ConfigurableEnvironment)
             * @see SpringApplication#printBanner(ConfigurableEnvironment)
             */
            context=createApplicationContext();
            // 异常报告
            exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
                    new Class[]{ConfigurableApplicationContext.class}, context);

            /**
             * 准备容器
             * @see SpringApplication#prepareContext(ConfigurableApplicationContext, ConfigurableEnvironment, org.springframework.boot.SpringApplicationRunListeners, ApplicationArguments, Banner)
             *
             * 刷新容器
             * @see SpringApplication#refresh(ApplicationContext)
             *
             * 刷新容器后做的一些事
             * @see SpringApplication#afterRefresh(ConfigurableApplicationContext, ApplicationArguments)
             */
            stopWatch.stop();
            // 开始阶段结束
            runListener.started(context);
            /**
             * @see SpringApplication#callRunner(ApplicationRunner, ApplicationArguments)
             */


        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        // 开始运行
        try {
            runListener.running(context);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return context;

    }

    /**
     * 根据启动参数和运行时监听器一起获取环境
     * @param applicationArguments
     * @param runListener
     */
    private ConfigurableEnvironment prepareEnvironment(DefaultApplicationArguments applicationArguments, SpringApplicationRunListeners runListener) {
        ConfigurableEnvironment environment = this.environment;
        if (environment != null) {
            return environment;
        }
        switch (getWebApplicationType()) {
            case SERVLET:
                environment = new StandardServletEnvironment();
                break;
            case REACTIVE:
                environment = new StandardReactiveWebEnvironment();
                break;
            case NONE:
                environment = new StandardEnvironment();
                break;
        }
        /**
         * 原理：设置一个提供转换服务的对象{@link org.springframework.boot.convert.ApplicationConversionService}
         * 用的是一个单例对象，volatile双重检查锁定
         * @see ApplicationConversionService#getSharedInstance()
         *
         * 然后去解析参数
         * @see SimpleCommandLineArgsParser#parse(String...)
         */
        configureEnvironment(environment, applicationArguments.getSourceArgs());
        //
        runListener.environmentPrepared(environment);
        if (!this.isCustomEnvironment) {
            environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
                    deduceEnvironmentClass());
        }
        ConfigurationPropertySources.attach(environment);
        return environment;

    }

    private Class<? extends StandardEnvironment> deduceEnvironmentClass() {
        switch (getWebApplicationType()) {
            case SERVLET:
                return StandardServletEnvironment.class;
            case REACTIVE:
                return StandardReactiveWebEnvironment.class;
            case NONE:
                return StandardEnvironment.class;
        }
    }

    /**
     * 获取运行时候的监听器
     */
    private SpringApplicationRunListeners getRunListener(String[] args) {
        Collection<? extends SpringApplicationRunListener> applicatonListener = getSpringFactoriesInstances(SpringApplicationRunListener.class,
                new Class[]{SpringApplication.class, String[].class},
                this, args);
        return new SpringApplicationRunListeners(getApplicationLog(), applicatonListener);
    }

    /**
     * 以重在缺少显示屏，键盘或者鼠标时候的配置，也就是Web服务
     */
    private void configureHeadlessProperty() {
        System.setProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS,
                System.getProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, Boolean.toString(this.headless)));
    }

    /**
     * 是的 spring/factories 文件转化为运行时的一个结构，并且放入缓存当中
     * 然后通过发射常见对应的对象集合
     * @param type
     * @param parameterType
     * @param args
     * @param <T>
     * @return
     */
    public <T> Collection<T> getSpringFactoriesInstances(Class<?> type,
                                                         Class<?>[] parameterType,
                                                         Object... args) {
        String factoryName = type.getName();
        ClassLoader classLoader = getClassLoader();
        // 获取名称
        List<String> nameList = SpringFactoryLoaderImpl.loadSpringFactories(classLoader)
                .getOrDefault(factoryName, Collections.EMPTY_LIST);
        LinkedHashSet<String> names = new LinkedHashSet<>(nameList);
        ArrayList<T> instances = new ArrayList<>(names.size());
        // 实例化对象
        for (String name : names) {
            try {
                Class<?> instanceClass = ClassUtils.forName(name, classLoader);
                // 判断是否合法
                Assert.isAssignable(type, instanceClass);
                Constructor<?> constructor = instanceClass.getConstructor(parameterType);
                T instance = (T)BeanUtils.instantiateClass(constructor, args);
                instances.add(instance);
            } catch (Throwable ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        AnnotationAwareOrderComparator.sort(instances);
        return instances;
    }
}
