package org.sekiro.framework.mvc.servlet;

import com.sun.deploy.net.HttpRequest;
import org.sekiro.framework.annotation.Controller;
import org.sekiro.framework.annotation.RequestMapping;
import org.sekiro.framework.annotation.RequestParam;
import org.sekiro.framework.aop.AopProxyUtils;
import org.sekiro.framework.context.ApplicationContext;
import org.sekiro.framework.mvc.HandlerAdapter;
import org.sekiro.framework.mvc.HandlerMapping;
import org.sekiro.framework.mvc.ModelAndView;
import org.sekiro.framework.mvc.ViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Map<HandlerMapping,HandlerAdapter> handlerAdapters = new HashMap<>();

    private List<ViewResolver> viewResolvers = new ArrayList<>();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //根据用户请求的url获取一个handler
        HandlerMapping handler = getHandler(req);
        if(handler == null) {
            resp.getWriter().write("404 not found");
            return;
        }
        HandlerAdapter ha = getHandlerAdapter(handler);
        ModelAndView mv = ha.handle(req,resp,handler);
        processDispatchResult(resp,mv);
    }

    private void processDispatchResult(HttpServletResponse resp, ModelAndView mv) throws Exception {
        //调用viewResolver的resolveView方法
        if(null == mv) return;
        if(this.viewResolvers.isEmpty()) return;
        for (ViewResolver viewResolver : viewResolvers) {
            if(!mv.getViewName().equals(viewResolver.getViewName())) continue;
            String out = viewResolver.viewResolver(mv);
            if(out != null) {
                resp.getWriter().write(out);
                break;
            }
        }

    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        if(this.handlerAdapters.isEmpty())
            return null;
        return handlerAdapters.get(handler);
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()) return null;
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");

        for (HandlerMapping handlerMapping : handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if(!matcher.matches())
                continue;
            return handlerMapping;

        }
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
        String[] beanNames = context.getBeanDefinitionNames();
        try {


            //首先从容器中取到所有实例
            for (String beanName : beanNames) {
                Object proxy = context.getBean(beanName);
                Object instance = AopProxyUtils.getTargetObject(proxy);
                Class<?> clazz = instance.getClass();
                //如果不是Controller就继续循环
                if (!clazz.isAnnotationPresent(Controller.class))
                    continue;
                String baseUrl = "";
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    baseUrl = requestMapping.value();
                }
                //扫描所有的public方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(RequestMapping.class))
                        continue;
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new HandlerMapping(instance, method, pattern));
                    System.out.println("mapping :" + regex + " , " + method);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
    //HandlerAdapter的作用是动态配置参数匹配Method
    //因此初始化要做的就是将这些Method参数的名字或者类型按照一定的顺序保存
    //可以通过记录这些参数的位置index，挨个从数组中填值，这样就和参数的顺序无关了
    private void initHandlerAdapters(ApplicationContext context) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            //每一个方法有一个参数列表
            Map<String,Integer> paramMapping = new HashMap<>();
            //Method的每一个参数都有可能加多个注解，因此是二维数组
            Annotation[][] parameterAnnotations = handlerMapping.getMethod().getParameterAnnotations();

            //对加了RequestParam的参数进行处理
            for (int i = 0; i < parameterAnnotations.length; i++) {
                for (Annotation annotation : parameterAnnotations[i]) {
                    if(annotation instanceof RequestParam) {
                        String paramName = ((RequestParam) annotation).value().trim();
                        //如果注解RequestParam的值不为空
                        if(!"".equals(paramName)) {
                            paramMapping.put(paramName,i);
                        }
                    }
                }
            }
            //处理没有加RequestParam的参数
            //比如HttpServletRequest或Response
            Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if(parameterType == HttpServletRequest.class ||
                   parameterType == HttpServletResponse.class){
                    paramMapping.put(parameterType.getName(),i);
                }
            }

            handlerAdapters.put(handlerMapping,new HandlerAdapter(paramMapping));
        }
    }


    private void initViewResolvers(ApplicationContext context) {
        //解决页面名字和模板关联的问题
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);
        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new ViewResolver(template.getName(),template));
        }

    }


    private void initLocaleResolver(ApplicationContext context) {}

    private void initThemeResolver(ApplicationContext context) {}

    private void initRequestToViewNameTranslator(ApplicationContext context) {}

    private void initHandlerExceptionResolvers(ApplicationContext context) {}

    private void initFlashMapManager(ApplicationContext context) {}

    private void initMultipartResolver(ApplicationContext context) {}


}
