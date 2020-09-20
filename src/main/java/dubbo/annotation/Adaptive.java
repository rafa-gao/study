package dubbo.annotation;

import java.lang.annotation.*;

/**
 * @author rafa gao
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Adaptive {

    String[] value() default {};
}
