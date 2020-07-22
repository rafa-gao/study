package mybatis.plugintest;

/**
 * @author rafa gao
 */


public class Container {

    private InterceptorChain interceptorChain = new InterceptorChain();
    private static final String INTERCEPTOR_NAME = "mybatis.plugintest.InterceptorUse";

    public Container() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // 收集所有的拦截器，为了简化，这里采取手动的收集
        Interceptor interceptorInstance = (Interceptor) resolveAlias(INTERCEPTOR_NAME).newInstance();
        interceptorChain.add(interceptorInstance);
    }

    private <T> Class<T> resolveAlias(String name) throws ClassNotFoundException {
        return (Class<T>) Class.forName(name, false, getClass().getClassLoader());
    }

    public Executor newExecutor() {
        // 新建查询器
        Executor executor = new ExecutorImpl();
        // 实现插件的插入 主要是实现动态代理
        return (Executor) interceptorChain.pluginAll(executor);
    }

}
