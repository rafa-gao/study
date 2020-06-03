package dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author rafa gao
 */
public class InvocationHandlerImpl implements InvocationHandler {


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getName() == "methodA") {
            System.out.println("methodA方法前置通知----------");

            System.out.println("methodA方法后置通知----------");
        }
        return null;
    }
}
