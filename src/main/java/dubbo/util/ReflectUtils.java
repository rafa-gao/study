package dubbo.util;

import java.util.Date;

/**
 * @author rafa gao
 */


public class ReflectUtils {


    public static boolean isPrimitives(Class<?> clazz) {
        if (clazz.isArray()) {
            return isPrimitive(clazz.getComponentType());
        }
        return isPrimitive(clazz);
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }




}
