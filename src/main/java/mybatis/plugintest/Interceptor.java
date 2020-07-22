package mybatis.plugintest;

import java.lang.reflect.Method;

/**
 * @author rafa gao
 */
public interface Interceptor {

    /**
     * 实现的拦截方法，这个方法是最核心的方法，一些拦截之后的逻辑主要就是靠这个方法实现的
     *
     * @return 调用后返回值
     * @param target
     * @param method
     * @param args
     */
    Object intercept(Object target, Method method, Object[] args);

    /**
     * 具体的装配方法，就是将对象装配到拦截对象的方法上面
     * 默认实现是采用{@link Plugin#wrap()} 实现的动态代理
     */
    Object plugin(Object target);
}
