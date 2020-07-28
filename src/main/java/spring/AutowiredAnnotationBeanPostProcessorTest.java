package spring;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author rafa gao
 */


public class AutowiredAnnotationBeanPostProcessorTest extends AutowiredAnnotationBeanPostProcessor {
    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>(4);
    private String requiredParameterName = "required";



    private InjectionMetadata parseAutowiringMetadate(final Class<?> clazz) {
        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;
        do {

            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

            // 寻找field注入
            ReflectionUtils.doWithFields(clazz, (field) -> {
                Annotation[] annotations = field.getAnnotations();
                if (annotations.length > 0) {
                    // 使用目标注解挨个比较
                    for (Class<? extends Annotation> type : autowiredAnnotationTypes) {
                        // 这里就为了判断一下是否时必须要注入的？？？ 好麻烦
                        AnnotationAttributes annotationAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(field, type);
                        if (annotationAttributes != null) {
                            // static不能被注入
                            if (Modifier.isStatic(field.getModifiers())) {
                                return;
                            }
                            boolean required = isRequired(annotationAttributes);
                            elements.add(new AutowiredFieldElement(field, required));
                        }
                    }
                }
            });
            // 寻找set方法注入
            ReflectionUtils.doWithMethods(clazz, (method)->{
                // 寻找被桥接的方法
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                if (BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                for (Class<? extends Annotation> type : autowiredAnnotationTypes) {
                    AnnotationAttributes annotationAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(bridgedMethod, type);
                    // 该方法必须没有被重写
                    if (annotationAttributes != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                        // 静态不行
                        if (Modifier.isStatic(method.getModifiers())) {
                            return;
                        }
                        // 无属性自然是不能注入的
                        if (method.getParameterCount() == 0) {
                            return;
                        }
                        boolean required = isRequired(annotationAttributes);
                        // 获取get、set方法
                        PropertyDescriptor propertyForMethod = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                        currElements.add(new AutowiredMethodElement(method, required, propertyForMethod));
                    }
                }
            });

            // 为什么要把父类属性的注入放在前面？？
            elements.addAll(0, currElements);
            // 寻找set 方法注入
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
        // 哪一个class的注入元素集合
        return new InjectionMetadata(clazz, elements);
    }

    private boolean isRequired(AnnotationAttributes attributes) {
        String requiredParameterName = this.requiredParameterName;
        // 不包含默认是必要的
        return !attributes.containsKey(requiredParameterName) || attributes.getBoolean(requiredParameterName);
    }


    private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

        private final boolean required;

        private volatile boolean cached = false;

        @Nullable
        private volatile Object cachedFieldValue;

        public AutowiredFieldElement(Field field, boolean required) {
            super(field, null);
            this.required = required;
        }

        @Override
        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            return;
        }
    }

    private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {

        private final boolean required;

        private volatile boolean cached = false;

        @Nullable
        private volatile Object[] cachedMethodArguments;

        public AutowiredMethodElement(Method method, boolean required, @Nullable PropertyDescriptor pd) {
            super(method, pd);
            this.required = required;
        }

        @Override
        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            return;
        }

        @Nullable
        private Object[] resolveCachedArguments(@Nullable String beanName) {
            Object[] cachedMethodArguments = this.cachedMethodArguments;
            if (cachedMethodArguments == null) {
                return null;
            }
            Object[] arguments = new Object[cachedMethodArguments.length];
//            for (int i = 0; i < arguments.length; i++) {
//                arguments[i] = resolvedCachedArgument(beanName, cachedMethodArguments[i]);
//            }
            return arguments;
        }
    }




}
