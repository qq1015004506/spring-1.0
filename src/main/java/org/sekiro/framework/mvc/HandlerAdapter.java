package org.sekiro.framework.mvc;

import org.omg.PortableInterceptor.INACTIVE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 21:50
 * email       : 1015004506@qq.com
 * description :
 *     根据参数匹配调用的方法
 */

public class HandlerAdapter {


    private Map<String, Integer> paramMapping;

    public HandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    //为什么要把handler传进来
    //因为handler中包含了controller method url信息
    //根据用户请求的参数信息，跟method中的参数信息进行动态匹配
    //因为controller中任何方法都可以加入req 和 resp
    //但是req resp 不可以new 出来
    //因此handler方法的req resp参数只是为了将其赋值给处理请求方法的参数
    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) throws Exception {

        //1.准备好这个方法的参数列表
        //方法重载：参数的个数，参数的类型，参数顺序，方法的名字
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();

        //2.拿到自定义的命名参数列表位置
        //用户通过url传过来的参数
        Map<String, String[]> reqParameterMap = req.getParameterMap();
        //3.构造实参列表
        Object[] paramValues = new Object[paramTypes.length];
        for (Map.Entry<String, String[]> param : reqParameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s","");
            //如果这个参数不在paramMapping中
            //类似于传了很多参数，但是我只要其中的几个
            //就继续进行下一次循环
            if(!this.paramMapping.containsKey(param.getKey())) continue;
            Integer index = paramMapping.get(param.getKey());
            //因为getParameterMap返回的map都是String类型的
            //因此要针对传过来的参数进行转换
            paramValues[index] = castStringValue(value,paramTypes[index]);
        }
        if(this.paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }
        if(this.paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }


        //4.从handler中取出controller和method，利用反射机制进行调用

        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        if(result == null) return null;
        if(result.getClass() == handler.getMethod().getReturnType()) {
            return (ModelAndView) result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> clazz) {
        if(clazz == String.class) {
            return value;
        }else if(clazz == Integer.class) {
            return Integer.valueOf(value);
        }else if(clazz == int.class) {
            return Integer.valueOf(value).intValue();
        }else {
            //...还有很多
            return null;
        }
    }
}
