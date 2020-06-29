package spring;

import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author rafa gao
 */
public class SpringMVCTest {


    /**
     * spring流程分析
     * 用户的请求首先被发送到前端控制器，由前端控制器决定请求最终被发给哪一个Controller处理
     * @see org.springframework.web.servlet.DispatcherServlet#doDispatch(HttpServletRequest, HttpServletResponse)
     *  1、检查是否存在文件上传 {@link org.springframework.web.servlet.DispatcherServlet#checkMultipart(HttpServletRequest)}
     *  2、从 response 中获取对应的 mappedHandler {@link org.springframework.web.servlet.DispatcherServlet#getHandler(HttpServletRequest)}
     *     这些 Handlers 全部被存储在一个集合当中 {@link org.springframework.web.servlet.DispatcherServlet#handlerMappings}
     *  3、获取对应的 HandlerAdapter {@link org.springframework.web.servlet.DispatcherServlet#getHandlerAdapter(Object)}
     *     这些 HandlerAdapter 同样存放在集合当中 {@link org.springframework.web.servlet.DispatcherServlet#handlerAdapters}
     *  4、最终调用 {@link org.springframework.web.servlet.HandlerAdapter#handle(HttpServletRequest, HttpServletResponse, Object)} 方法去执行
     *  5、拿到返回的ModelAndView交给试图解析器去填充数据
     *  6、最终将得到的数据返回给用户
     */
    @SuppressWarnings("JavadocReference")
    public void springMvcAnalyze(){

    }

    /**
     * handlerMappings的初始化解析
     * 入口方法在 {@link org.springframework.web.servlet.DispatcherServlet#initHandlerMappings(ApplicationContext)}
     */
    @SuppressWarnings("JavadocReference")
    public void handlerMappingsInit() {

    }
}
