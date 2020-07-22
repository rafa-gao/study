package mybatis.plugintest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author rafa gao
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    // 代表的类
    Class<?> type();

    // 代表的方()名称
    String method();

    // 代表的方法参数
    Class<?>[] args();

}
