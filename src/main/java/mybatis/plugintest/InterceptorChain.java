package mybatis.plugintest;

import java.util.ArrayList;

/**
 * 将所有的拦截器收集起来
 *
 * @author rafa gao
 */
public class InterceptorChain extends ArrayList<Interceptor> {

//    private Map<Class<?>, Set<Method>>


    // 装配所有的插件，原理是使用多次动态代理
    public Object pluginAll(Object target) {
        for (Interceptor interceptor : this) {
           target = interceptor.plugin(target);
        }
        return target;
    }

}
