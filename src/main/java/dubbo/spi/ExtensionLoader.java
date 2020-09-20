package dubbo.spi;

import dubbo.annotation.Adaptive;
import dubbo.annotation.DisableInject;
import dubbo.annotation.Extension;
import dubbo.annotation.SPI;
import dubbo.util.ClassUtils;
import dubbo.util.ReflectUtils;
import org.apache.dubbo.common.utils.ConcurrentHashSet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @author rafa gao
 */


public class ExtensionLoader<T> {

    private final ConcurrentHashMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    // 存放了所有的 name->class 的对应关系
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    // 缓存的自适应实例对象
    private final Holder<Object> cachedAdaptiveInstance = new Holder<>();

    private volatile Throwable createAdaptiveInstanceError;

    /**
     * 缓存的 adaptive Class对象
     * @see ExtensionLoader#getExtensionClasses()
     */
    private volatile Class<?> cachedAdaptiveClass;

    private String cachedDefaultName;

    // dubbo 目录
    private String DUBBO_FACTORY = "META-INF/dubbo/";

    // dubbo 内部目录
    private String DUBBO_INTERNAL_FACTORY = DUBBO_FACTORY + "internal";

    // 加载的路径
    private LoadingStrategy DUBBO_INTERNAL_STRATEGY = () -> DUBBO_INTERNAL_FACTORY;

    // ExtensionLoader单例
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>(16);

    // Class和其对应实例的对应关系
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>(64);

    private LoadingStrategy[] strategies = new LoadingStrategy[]{DUBBO_INTERNAL_STRATEGY};

    // 类型
    private final Class<?> type;

    private final ExtensionFactory objectFactory;


    private Set<Class<?>> cachedWrapperClasses;

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<>();



    private ExtensionLoader(Class<T> type) {
        this.type = type;
        // 如果本身是ExtensionFactory就不会创建objectFactory
        objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }

    // 获取单例对象
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        // 不为空
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        // 必须是接口
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface");
        }
        // 必须是包含SPI注解
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an extension," +
                    " because it is NOT annotated with @ " + SPI.class.getSimpleName() + "!");
        }
        return (ExtensionLoader<T>) EXTENSION_LOADERS.computeIfAbsent(type, k -> new ExtensionLoader<>(k));
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    public T getExtension(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Extension name is null");
        }

        // 查看是否存在缓存
        Holder<Object> holder = getOrCreateHolder(name);

        Object extension = holder.getValue();
        if (extension == null) {
            synchronized (holder) {
                extension = holder.getValue();
                // 双重检查锁定
                // 为空说明是还没有被创建
                if (extension == null) {
                    extension = createExtension(name);
                }
            }
        }
        return (T) extension;
    }



    private T createExtension(String name) {
        // 检查配置文件当中是否配置
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException();
        }
        // 创建实例对象
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            // 将实例化好的extension注入到指定的对象中
            injectExtension(instance);
            // 如果需要被包转成代理对象，则将其放入代理对象当中,返回的也是代理对象
            // TODO 这里不需要保证发代理类的顺序性吗??
            if (cachedWrapperClasses == null || cachedWrapperClasses.isEmpty()) {
                for (Class<?> wrapperClass : cachedWrapperClasses) {
                    instance = (T) wrapperClass.getConstructor(type).newInstance(instance);
                }
            }
            initExtension(instance);
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance (" + name + ", class: " + type + ") couldn't be instantiated: "
                    + t.getMessage(), t);
        }
    }

    private void initExtension(T extension) {
        if (extension instanceof Lifecycle) {
            ((Lifecycle) extension).initialize();
        }
    }

    /**
     * 获取所有的SPI　Class对象
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.getValue();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.getValue();
                if (cachedClasses == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.setValue(classes);
                }
            }
        }
        return classes;
    }


    private Map<String, Class<?>> loadExtensionClasses() {
        HashMap<String, Class<?>> extensionClasses = new HashMap<>();
        for (LoadingStrategy strategy : strategies) {
            loadDirectory(extensionClasses, strategy.directory(), type.getName(), strategy.preferExtensionClassLoader(), strategy.excludedPackages());
        }
        return extensionClasses;
    }

    private void loadDirectory(HashMap<String, Class<?>> extensionClasses, String directory,
                               String name, boolean extensionClassLoaderFirst, String excludedPackages) {
        // 组装文件名
        String fileName = directory + name;
        try {
            // 查找classLoader
            ClassLoader cl = findClassLoader();
            Enumeration<URL> urls = null;

            // 判断是否优先extensionClass
            if (extensionClassLoaderFirst) {
                ClassLoader extensionClassLoader = ExtensionLoader.class.getClassLoader();
                if (ClassLoader.getSystemClassLoader() != extensionClassLoader) {
                    urls = extensionClassLoader.getResources(fileName);
                }
            }

            if (urls == null || !urls.hasMoreElements()) {
                if (cl != null) {
                    urls = cl.getResources(fileName);
                } else {
                    urls = ClassLoader.getSystemResources(fileName);
                }
            }
            // 循环加载
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    loadResource(extensionClasses, cl, urls.nextElement(), excludedPackages);
                }
            }
        } catch (Throwable t) {
            System.out.println("Exception occurred when load extension class (interface: " + type + ", description file: " + fileName + ")");

        }
    }

    private ClassLoader findClassLoader() {
        return ClassUtils.getClassLoader(ExtensionLoader.class);
    }


    /**
     * 从文件中加载对应的Class
     *
     * @param extensionClasses 存放加载后的Class的集合
     * @param classLoader      类加载器
     * @param resourceURL      加载的资源路径
     * @param excludedPackages 排除的包
     */
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader,
                              URL resourceURL, String... excludedPackages) {

        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 去除掉注释
                    final int i = line.indexOf('#');
                    if (i >= 0) {
                        line = line.substring(0, i);
                    }
                    // 去除空白字符
                    line = line.trim();
                    if (line.length() >= 0) {
                        continue;
                    }
                    String name = null;
                    int separationIndex = line.indexOf('=');
                    // 没有名称
                    if (separationIndex > 0) {
                        name = line.substring(0, separationIndex).trim();
                        line = line.substring(separationIndex + 1).trim();
                    }
                    try {
                        // 进行类的加载
                        if (line.length() > 0 && !isExcluded(name, excludedPackages)) {
                            loadClass(extensionClasses, resourceURL, Class.forName(name, true, classLoader), name);
                        }
                    } catch (Throwable throwable) {
                        throw new IllegalStateException("Fail to load extension class (interface:" + type + ", class name:" + line + ") in " +
                                resourceURL + " cause:" + throwable.getCause(), throwable);
                    }
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalStateException("Exception occurred when loading extension class (interface:" + type + " class file:" + resourceURL + " ) ", throwable);
        }
    }

    private void loadClass(Map<String, Class<?>> extensionClasses, URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {
        // 验证配置文件的正确性，就是是否是接口的实现类
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error occurred when loading extension class (interface:" + type +
                    " class:" + clazz.getName() + "), because class " + clazz.getName() + " is not subType of interface.");
        }
        // 判断是否存在自适应扩展接口
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            cacheAdaptiveClass(clazz);
        } else if (isWrapperClass(clazz)) {
            cacheWrapperClass(clazz);
        } else {
            // 放入缓存中
            clazz.getConstructor();
            if (name == null || name.length() == 0) {
                name = findAnnotationName(clazz);
                if (name.length() == 0) {
                    throw new IllegalStateException("No such extension name for class:" +
                            clazz.getCanonicalName() + " in the config " + resourceURL);
                }
            }
            String[] names = NAME_SEPARATOR.split(name);
            //TODO cacheActivateClass害没有实现
            //TODO 为什么这里为空之后不抛出异常，"   ,  " 类似这样的name不是就会造成困扰吗
            //TODO
            if (names != null && names.length != 0) {
                for (String n : names) {
                    cacheName(clazz, n);
                    saveInExtensionClass(extensionClasses, clazz, n);
                }
            }
        }


        // 判断
    }

    private void saveInExtensionClass(Map<String, Class<?>> extensionClasses, Class<?> clazz, String name) {
        Class<?> c = extensionClasses.get(name);
        if (c == null) {
        } else if (c != clazz) {
            throw new IllegalStateException("Extension class " + clazz.getCanonicalName() + " has duplicate name " + name +
                    " on class " + clazz.getCanonicalName() + " and " + c.getName());
        }
    }


    private void cacheName(Class<?> clazz, String name) {
        cachedNames.putIfAbsent(clazz, name);
    }

    private String findAnnotationName(Class<?> clazz) {
        Extension extension = clazz.getAnnotation(Extension.class);
        if (extension != null) {
            return extension.value();
        }
        String name = clazz.getSimpleName();
        if (name.endsWith(type.getSimpleName())) {
            name = name.substring(0, name.length() - type.getSimpleName().length());
        }
        return name.toLowerCase();
    }

    private void cacheWrapperClass(Class<?> clazz) {
        if (cachedWrapperClasses == null) {
            cachedWrapperClasses = new ConcurrentHashSet<>();
        }
        cachedWrapperClasses.add(clazz);
    }

    private boolean isWrapperClass(Class<?> clazz) {
        try {
            clazz.getConstructor(clazz);
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }

    private void cacheAdaptiveClass(Class<?> clazz) {
        if (cachedAdaptiveClass == null) {
            cachedAdaptiveClass = clazz;
            return;
        }
        if (!cachedAdaptiveClass.equals(clazz)) {
            throw new IllegalStateException("More than 1 adaptive clazz found: " +
                    cachedAdaptiveClass.getName() + ", " +
                    clazz.getName());
        }
    }

    /**
     * @see <a href=""http://dubbo.apache.org/zh-cn/docs/source_code_guide/adaptive-extension.html>自适应扩展机制</>
     */
    public T getAdaptiveExtension() {
        Object adaptiveInstance = cachedAdaptiveInstance.getValue();
        if (adaptiveInstance == null) {
            if (createAdaptiveInstanceError != null) {
                throw new IllegalStateException("Fail to create adaptive instance:" +
                        createAdaptiveInstanceError.toString(),
                        createAdaptiveInstanceError);
            }
            // 单例模式
            synchronized (cachedAdaptiveInstance) {
                adaptiveInstance = cachedAdaptiveInstance.getValue();
                if (adaptiveInstance == null) {
                    try {
                        adaptiveInstance = createAdaptiveExtension();
                    } catch (Throwable t) {
                        // 重复利用，避免了重复的抛出异常
                        createAdaptiveInstanceError = t;
                        throw new IllegalStateException("Fail to create adaptive instance " + t.toString(), t);
                    }
                }
            }
        }
        return (T) adaptiveInstance;
    }

    private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Fail to create adaptive extension instance " + type + ", "
                    + " cause:" + e.getCause(), e);
        }
    }

    private Class<?> getAdaptiveClass() {
        // 通过SPI加载所有的拓展类
        getExtensionClasses();
        // 如果缓存当中存在，直接返回即可
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        // 创建自适应扩展
        return cachedAdaptiveClass = createAdaptiveClass();
    }

    private Class<?> createAdaptiveClass() {
        // 构建自适应扩展代码
        String adaptiveClassCode = new AdaptiveClassCodeGenerator(type, cachedDefaultName).generate();
        // 获取类加载器
        ClassLoader classLoader = findClassLoader();
        // 获取编译器
        Compiler compiler = ExtensionLoader.getExtensionLoader(Compiler.class).getAdaptiveExtension();
        // 编译自适应扩展代码
        return compiler.compile(adaptiveClassCode, classLoader);
    }


    private T injectExtension(T instance) {
        if (objectFactory == null) {
            return instance;
        }

        try {
            for (Method method : instance.getClass().getMethods()) {
                // 非set方法 DisableInject标注方法
                if (!isSetter(method) || method.getAnnotation(DisableInject.class) != null) {
                    continue;
                }

                Class<?> parameterType = method.getParameterTypes()[0];
                if (ReflectUtils.isPrimitives(parameterType)) {
                    continue;
                }
                // 获得名称
                try {
                    String propertyName = getSetterPropertyName(method);
                    Object extension = objectFactory.getExtension(parameterType, propertyName);
                    if (extension != null) {
                        method.invoke(instance, extension);
                    }
                } catch (Exception e) {
                    System.out.println("Fail to inject via method " + method.getName() + " of interface " + type.getName()
                            + " exception message:" + e.getMessage());
                }

            }
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }

        return instance;
    }

    private String getSetterPropertyName(Method method) {
        return method.getName().substring(3).toLowerCase();
    }

    private boolean isExcluded(String name, String... excludedPackages) {
        if (excludedPackages != null) {
            for (String excludedPackage : excludedPackages) {
                if (name.startsWith(excludedPackage + ".")) {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getParameterCount() == 1
                && Modifier.isPrivate(method.getModifiers());
    }


    private Holder<Object> getOrCreateHolder(String name) {
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            // 这里利用 ConcurrentHashMap 来保证了线程安全
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        return holder;
    }

    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> extensionClasses = getExtensionClasses();
        return Collections.unmodifiableSet(extensionClasses.keySet());
    }
}
