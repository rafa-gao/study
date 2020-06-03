package dynamicproxy;

import sun.misc.ProxyGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author rafa gao
 *
 * jdk动态代理测试类
 *
 * 动态代理实现原理几个重要的方法
 *
 * @see Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler) 入口方法
 * @see Proxy#getProxyClass0(ClassLoader, Class[]) 生成动态代理的class文件
 * @see java.lang.reflect.Proxy.ProxyClassFactory#apply(ClassLoader, Class[]) 主要是确定包名和class名称
 * @see ProxyGenerator#generateProxyClass(String, Class[], int) 通过上一步的文件名，“拼凑”出最终的byte[]数组形式的class字节流
 *      本来的话，我们通过类加载器去拿到二进制的字节流，但是在这里，我们生产出了二进制字节流
 * @see Proxy#defineClass0(ClassLoader, String, byte[], int, int) JNI接口创造出真正形式的class对象
 * @see Class#getConstructor(Class[]) 拿到{@link InvocationHandler}的class对象作为参数的构造器
 * @see java.lang.reflect.Constructor#newInstance(Object...) 实参就是我们写的的{@link InvocationHandler}具体实现类
 */
@SuppressWarnings("JavadocReference")
public class JdkDynamicTest {

    /**
     * 产生代理类的class文件
     * 该方法还可以通过设置虚拟机参数 -Dsun.misc.ProxyGenerator.saveGeneratedFiles=true 生成.class文件
     */
    private static void generateJdkDyncProxyFile(){
        String className = "BaseProxy";
        String path = "./src/main/java/dynamicproxy/";
        byte[] bytes = ProxyGenerator.generateProxyClass(className, new Class[]{Base.class});
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(path+className+".class");
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("旧的.class文件已经被删除");
                }
            }
            fileOutputStream = new FileOutputStream(path+className+".class");
            fileOutputStream.write(bytes);
            System.out.println("新的.class文件已经生成");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException ignore) {

            }
        }

    }

    /**
     * 最终生成的类是 com.sum.$proxyXXX.class
     */
    private static void generateJdkProxy() {
        Object proxy = Proxy.newProxyInstance(Base.class.getClassLoader(), new Class[]{Base.class}, new InvocationHandlerImpl());
        System.out.println(proxy.getClass().getCanonicalName());
    }

    /**
     * 最终生成的类是 dynamicproxy.$proxyXXX.class
     */
    private static void generateJdkProxyWithPrivateClass() {
        Object proxy = Proxy.newProxyInstance(Base.class.getClassLoader(), new Class[]{InnerBase.class}, new InvocationHandlerImpl());
        System.out.println(proxy.getClass().getCanonicalName());
    }

    private interface InnerBase {

    }


    public static void main(String[] args) {
        generateJdkDyncProxyFile();
        generateJdkProxy();
        generateJdkProxyWithPrivateClass();
    }
}
