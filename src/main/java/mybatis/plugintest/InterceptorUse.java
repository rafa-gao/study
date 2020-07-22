package mybatis.plugintest;

import java.lang.reflect.Method;

/**
 * 拦截器的实现类
 *
 * 这个类主要是使用者去实现的
 * @author rafa gao
 */
@Intercept(
        properties = {
            @Property(type = Executor.class,method = "query",args = {})
        }
)
public class InterceptorUse implements Interceptor{


        // 实现分页
        @Override
        public Object intercept(Object target, Method method, Object[] args) {
                System.out.println("执行查询前实现分页");
                Executor executor = (Executor) target;
                // 实现分页处理
                executor.query();

                return null;
        }

        @Override
        public Object plugin(Object target) {
                return Plugin.wrap(target, this);
        }

}
