package dubbo.annotation;

import java.lang.annotation.*;

/**
 * @author rafa gao
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Extension {

    String value() default "";
}
