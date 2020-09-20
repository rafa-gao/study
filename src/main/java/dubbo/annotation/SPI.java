package dubbo.annotation;

import java.lang.annotation.*;

/**
 * 扩展标记接口标志
 * 用于SPI　服务发现机制
 * @author rafa gao
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
