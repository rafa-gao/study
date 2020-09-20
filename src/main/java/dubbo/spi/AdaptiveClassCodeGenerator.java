package dubbo.spi;

import dubbo.annotation.Adaptive;
import dubbo.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 自适应类源码产生器
 *
 * @author rafa gao
 */
public class AdaptiveClassCodeGenerator {

    private static final String CODE_PACKAGE = "package %s;\n";

    private static final String CODE_IMPORTS = "import %s;\n";

    private static final String CODE_CLASS_DECLARATION = "public class %s$Adaptive implements %s {\n";

    private static final String CODE_METHOD_DECLARATION = "public %s %s(%s) %s {\n%s}\n";

    private static final String CODE_METHOD_ARGUMENT = "%s arg%d";

    private static final String CODE_METHOD_THROWS = "throws %s";

    private static final String CODE_UNSUPPORTED = "throw new UnsupportedOperationException(\"The method %s of interface %s is not adaptive method!\");\n";

    private static final String CODE_URL_NULL_CHECK = "if (arg%d == null) throw new IllegalArgumentException(\"url == null\");\n%s url = arg%d;\n";

    private static final String CLASSNAME_INVOCATION = "org.apache.dubbo.rpc.Invocation";

    private static final String CODE_INVOCATION_ARGUMENT_NULL_CHECK = "if (arg%d == null) throw new IllegalArgumentException(\"invocation == null\"); "
            + "String methodName = arg%d.getMethodName();\n";

    private static final String CODE_EXT_NAME_ASSIGNMENT = "String extName = %s;\n";

    private static final String CODE_EXT_NAME_NULL_CHECK = "if(extName == null) "
            + "throw new IllegalStateException(\"Failed to get extension (%s) name from url (\" + url.toString() + \") use keys(%s)\");\n";

    private static final String CODE_EXTENSION_ASSIGNMENT = "%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);\n";

    private static final String CODE_EXTENSION_METHOD_INVOKE_ARGUMENT = "arg%d";

    private final Class<?> type;

    private String defaultExtName;

    public AdaptiveClassCodeGenerator(Class<?> type, String defaultExtName) {
        this.type = type;
        this.defaultExtName = defaultExtName;
    }

    public String generate() {
        // 判断是否存在方法被Adaptive注解了
        if (!hasAdaptiveMethod(type)) {
            throw new IllegalStateException("Refuse to create class code, because " + type.getName() + " do NOT have adaptive method!");
        }
        // 模仿我们写代码时的操作
        StringBuilder classCodeBuilder = new StringBuilder();
        // 添加包名
        generatePackagesInfo(classCodeBuilder);
        // 添加import信息
        generateImportInfo(classCodeBuilder);
        // 添加类申明信息
        generateClassDeclaration(classCodeBuilder);

        // 最关键的，生成method的源代码
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            generateMethod(classCodeBuilder,method);
        }
        classCodeBuilder.append("}");
        return classCodeBuilder.toString();
    }

    private void generatePackagesInfo(StringBuilder classCodeBuilder) {
        classCodeBuilder.append(String.format(CODE_PACKAGE, type.getPackage()));
    }

    private void generateImportInfo(StringBuilder classCodeBuilder) {
        classCodeBuilder.append(String.format(CODE_IMPORTS, ExtensionLoader.class.getName()));
    }

    private void generateClassDeclaration(StringBuilder classCodeBuilder) {
        classCodeBuilder.append(String.format(CODE_CLASS_DECLARATION, type.getSimpleName(), type.getCanonicalName()));
    }

    private void generateMethod(StringBuilder classCodeBuilder, Method method) {
        // 构造方法的返回值
        String methodReturnType = method.getReturnType().getCanonicalName();
        // 构造方法名
        String methodName = method.getName();
        // 构造参数
        String methodArgs = generateMethodArgs(method);
        // 构造方法内容
        String methodContent = generateMethodContent(method);
        // 构造方法异常信息
        String methodThrows = generateMethodThrows(method);
        classCodeBuilder.append(String.format(CODE_METHOD_DECLARATION, methodName, methodReturnType, methodArgs, methodThrows, methodContent));
    }

    private String generateMethodThrows(Method method) {
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length > 0) {
            return String.format(CODE_METHOD_THROWS, Arrays.stream(exceptionTypes).map(Class::getCanonicalName).collect(Collectors.joining(", ")));
        } else {
            return "";
        }
    }

    private String generateMethodContent(Method method) {
        Adaptive adaptive = method.getAnnotation(Adaptive.class);
        // 非自适应扩展方法
        if (adaptive == null) {
            return generateUnsupported(method);
        } else {
            StringBuilder code = new StringBuilder();
            // 首先提取参数
            int urlTypeIndex = getUrlTypeIndex(method);

            // 不存在
            // 这一步结束之后，我么就可以获取到url参数了，名称为url
            if (urlTypeIndex == -1) {
                code.append(generateUrlAssignmentIndirectly(method));
            } else {
                code.append(generateUrlNullCheck(urlTypeIndex));
            }

            String[] values = getMethodAdaptiveValue(adaptive);
            // 从url中获取参数值


            boolean hasInvocation = hasInvocationArgument(method);

            code.append(generateInvocationArgumentNullCheck(method));

            code.append(generateExtNameAssignment(values, hasInvocation));
            // check extName == null?
            code.append(generateExtNameNullCheck(values));

            code.append(generateExtensionAssignment());

            // return statement
            code.append(generateReturnAndInvocation(method));
            return code.toString();
        }
    }

    private String generateReturnAndInvocation(Method method) {
        String returnStatement = method.getReturnType().equals(void.class) ? "" : "return ";

        String args = IntStream.range(0, method.getParameters().length)
                .mapToObj(i -> String.format(CODE_EXTENSION_METHOD_INVOKE_ARGUMENT, i))
                .collect(Collectors.joining(", "));

        return returnStatement + String.format("extension.%s(%s);\n", method.getName(), args);
    }


    private String generateExtensionAssignment() {
        return String.format(CODE_EXTENSION_ASSIGNMENT, type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
    }

    private String generateExtNameNullCheck(String[] value) {
        return String.format(CODE_EXT_NAME_NULL_CHECK, type.getName(), Arrays.toString(value));
    }

    // 头看昏了,555
    private String generateExtNameAssignment(String[] value, boolean hasInvocation) {
        String getNameCode = null;
        for (int i = value.length - 1; i >= 0; --i) {
            if (i == value.length - 1) {
                if (null != defaultExtName) {
                    if (!"protocol".equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                        }
                    } else {
                        getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                    }
                } else {
                    if (!"protocol".equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                        }
                    } else {
                        getNameCode = "url.getProtocol()";
                    }
                }
            } else {
                if (!"protocol".equals(value[i])) {
                    if (hasInvocation) {
                        getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                    } else {
                        getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                    }
                } else {
                    getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                }
            }
        }

        return String.format(CODE_EXT_NAME_ASSIGNMENT, getNameCode);
    }

    private String generateInvocationArgumentNullCheck(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return IntStream.range(0, pts.length).filter(i -> CLASSNAME_INVOCATION.equals(pts[i].getName()))
                .mapToObj(i -> String.format(CODE_INVOCATION_ARGUMENT_NULL_CHECK, i, i))
                .findFirst().orElse("");
    }

    private boolean hasInvocationArgument(Method method) {
        return Arrays.stream(method.getParameterTypes()).anyMatch((p) -> CLASSNAME_INVOCATION.equals(p.getName()));
    }

    /**
     * 寻找参数的对象是否才能在返回值为 {@link URL}的方法
     */
    private String generateUrlAssignmentIndirectly(Method method) {
        Class<?>[] pts = method.getParameterTypes();

        Map<String, Integer> getterReturnUrl = new HashMap<>();
        // find URL getter method
        for (int i = 0; i < pts.length; ++i) {
            for (Method m : pts[i].getMethods()) {
                String name = m.getName();
                if ((name.startsWith("get") || name.length() > 3)
                        && Modifier.isPublic(m.getModifiers())
                        && !Modifier.isStatic(m.getModifiers())
                        && m.getParameterTypes().length == 0
                        && m.getReturnType() == org.apache.dubbo.common.URL.class) {
                    getterReturnUrl.put(name, i);
                }
            }
        }

        if (getterReturnUrl.size() <= 0) {
            // getter method not found, throw
            throw new IllegalStateException("Failed to create adaptive class for interface " + type.getName()
                    + ": not found url parameter or url attribute in parameters of method " + method.getName());
        }

        Integer index = getterReturnUrl.get("getUrl");
        if (index != null) {
            return generateGetUrlNullCheck(index, pts[index], "getUrl");
        } else {
            Map.Entry<String, Integer> entry = getterReturnUrl.entrySet().iterator().next();
            return generateGetUrlNullCheck(entry.getValue(), pts[entry.getValue()], entry.getKey());
        }
    }

    /**
     * 1, test if argi is null
     * 2, test if argi.getXX() returns null
     * 3, assign url with argi.getXX()
     */
    private String generateGetUrlNullCheck(int index, Class<?> type, String method) {
        // Null point check
        StringBuilder code = new StringBuilder();
        code.append(String.format("if (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");\n",
                index, type.getName()));
        code.append(String.format("if (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");\n",
                index, method, type.getName(), method));
        code.append(String.format("%s url = arg%d.%s();\n", org.apache.dubbo.common.URL.class.getName(), index, method));
        return code.toString();
    }

    private String generateUrlNullCheck(int urlTypeIndex) {
        return String.format(CODE_URL_NULL_CHECK, urlTypeIndex, URL.class.getCanonicalName(), urlTypeIndex);
    }


    private String[] getMethodAdaptiveValue(Adaptive adaptive) {
        String[] value = adaptive.value();
        if (value.length == 0) {
            value = new String[]{StringUtils.camelToSplitName(type.getSimpleName(), ".")};
        }
        return value;
    }

    private int getUrlTypeIndex(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (URL.class.equals(parameterTypes[i])) {
                return i;
            }
        }
        return -1;
    }

    private String generateUnsupported(Method method) {
        return String.format(CODE_UNSUPPORTED, method, type.getName());
    }

    private String generateMethodArgs(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return IntStream.range(0, parameterTypes.length - 1)
                .mapToObj(value -> String.format(CODE_METHOD_ARGUMENT, parameterTypes[value].getCanonicalName(), value))
                .collect(Collectors.joining(", "));
    }

    private boolean hasAdaptiveMethod(Class<?> type) {
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Adaptive.class)) {
                return true;
            }
        }
        return false;
    }
}
