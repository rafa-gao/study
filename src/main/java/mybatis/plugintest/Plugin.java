package mybatis.plugintest;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author rafa gao
 */
public class Plugin implements InvocationHandler {

    private Map<Class<?>, Set<Method>> propertyMap;

    private Interceptor interceptor;

    // 这里保留了target主要是为了实现更加细粒度的代理
    private Object target;


    public Plugin(Map<Class<?>, Set<Method>> propertyMap,Interceptor interceptor,Object target) {
        this.propertyMap = propertyMap;
        this.interceptor = interceptor;
        this.target = target;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Set<Method> methodSet = propertyMap.get(method.getDeclaringClass());
        if (!methodSet.isEmpty()&&methodSet.contains(method)) {
            return interceptor.intercept(target, method, args);
        }else {
            return method.invoke(proxy, args);
        }
    }

    /**
     * 给对象绑定插件
     * 主要是采用动态代理实现
     *
     * @param target      被绑的对象
     * @param interceptor 插件
     * @return 绑定好插件的对象
     */
    public static Object wrap(Object target, Interceptor interceptor) {
        // 判断对象是否符合要求
        Map<Class<?>, Set<Method>> propertyMap = getPropertyMap(interceptor);
        // 创建代理对象
        if (!propertyMap.isEmpty()) {
            Class<?> clz = target.getClass();
            return  Proxy.newProxyInstance(
                    clz.getClassLoader(),
                    clz.getInterfaces(),
                    new Plugin(propertyMap, interceptor, target));
        }
        return target;
    }

    public static Map<Class<?>, Set<Method>> getPropertyMap(Interceptor interceptor) {
        Class<? extends Interceptor> clz = interceptor.getClass();
        Intercept annotation = clz.getAnnotation(Intercept.class);
        if (annotation == null) {
            throw new RuntimeException("No @Intercepts annotation was found in interceptor" + clz.getName());
        }
        Property[] properties = annotation.properties();
        HashMap<Class<?>, Set<Method>> propertyMap = new HashMap<>();
        for (Property property : properties) {
            Class<?> type = property.type();
            String methodName = property.method();
            Class<?>[] args = property.args();

            Set<Method> methods = propertyMap.get(type);
            if (methods == null) {
                methods = new HashSet<>(properties.length);
                propertyMap.put(type, methods);
            }
            try {
                methods.add(type.getMethod(methodName, args));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Could not find method on " + type.getName() + " named " + type.getName() + ". Cause: " + e, e);
            }
        }
        return propertyMap;
    }

}
