package org.sekiro.framework.mvc.servlet;

import org.sekiro.framework.context.ApplicationContext;
import org.sekiro.framework.mvc.HandlerAdapter;
import org.sekiro.framework.mvc.HandlerMapping;
import org.sekiro.framework.mvc.ModelAndView;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 10:54
 * email       : 1015004506@qq.com
 * description :
 */

public class DispatcherServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    /*
     * 为什么用list 不用map ?
     * private Map<String, HandlerMapping> handlerMapping;
     * 因为@RequestMapping注解中可以存在正则表达式
     * 因此不能用一个url去匹配
     */

    private List<HandlerMapping> handlerMappings = new ArrayList<>();

    /*
     *
     */
    private List<HandlerAdapter> handlerAdapters = new ArrayList<>();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        String url = req.getRequestURI();
//        String contextPath = req.getContextPath();
//        url = url.replace(contextPath,"").replace("/+","/");
//        HandlerMapping handler = this.handlerMapping.get(url);
//        try {
//            ModelAndView mv = (ModelAndView)handler.getMethod().invoke(handler.getController());
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        doDispatch(req, resp);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        HandlerMapping handler = getHandler(req);
        HandlerAdapter ha = getHandlerAdapter(handler);
        ModelAndView mv = ha.handle(req,resp,handler);
        processDispatchResult(resp,mv);
    }

    private void processDispatchResult(HttpServletResponse resp, ModelAndView mv) {

    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        return null;
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        ApplicationContext context = new ApplicationContext(config.getInitParameter("contextConfigLocation"));
        initStrategies(context);
    }

    private void initStrategies(ApplicationContext context) {
        // 有九种策略
        // 针对于每个用户请求，都会经过一些处理的策略之后，最终才能有结果输出
        // 每种策略可以自定义干预，但是最终的结果都是一致
        // ModelAndView

        // =============  这里说的就是传说中的九大组件 ================

        initMultipartResolver(context);//文件上传解析，如果请求类型是multipart将通过MultipartResolver进行文件上传解析
        initLocaleResolver(context);//本地化解析
        initThemeResolver(context);//主题解析

        //HandlerMapping 用来保存Controller中配置的RequestMapping和Method的一个对应关系
        initHandlerMappings(context);//通过HandlerMapping，将请求映射到处理器

        //HandlerAdapters 用来动态匹配Method参数，包括类转换，动态赋值
        initHandlerAdapters(context);//通过HandlerAdapter进行多类型的参数动态匹配

        initHandlerExceptionResolvers(context);//如果执行过程中遇到异常，将交给HandlerExceptionResolver来解析
        initRequestToViewNameTranslator(context);//直接解析请求到视图名

        //通过ViewResolvers实现动态模板的解析W
        //自己解析一套模板语言
        initViewResolvers(context);//通过viewResolver解析逻辑视图到具体视图实现

        initFlashMapManager(context);//flash映射管理器


    }

    //将Controller中配置的RequestMapping和Method进行一一对应
    private void initHandlerMappings(ApplicationContext context) {

    }
    private void initHandlerAdapters(ApplicationContext context) {

    }


    private void initViewResolvers(ApplicationContext context) {

    }


    private void initLocaleResolver(ApplicationContext context) {}

    private void initThemeResolver(ApplicationContext context) {}

    private void initRequestToViewNameTranslator(ApplicationContext context) {}

    private void initHandlerExceptionResolvers(ApplicationContext context) {}

    private void initFlashMapManager(ApplicationContext context) {}

    private void initMultipartResolver(ApplicationContext context) {}


}
