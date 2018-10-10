package org.sekiro.framework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * author      : quzhiyu
 * date        : 2018/10/6 17:21
 * email       : 1015004506@qq.com
 * description :
 *     默认使用JDK动态代理
 */

public class AopProxy implements InvocationHandler {

    private AopConfig config;
    private Object target;

    public void setConfig(AopConfig config) {
        this.config = config;
    }


    //把原生的对象传进来
    public Object getProxy(Object instance){
        this.target = instance;
        Class<?> clazz = instance.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),this);
    }


    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {

        Method method = this.target.getClass().getMethod(m.getName(),m.getParameterTypes());


        //在原始方法调用前执行增强代码
        if(config.contains(method)){
            AopConfig.Aspect aspect = config.get(method);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }
        //反射调用原始代码
        Object result = method.invoke(this.target,args);

        //在原始方法调用后执行增强代码
        if(config.contains(method)) {
            AopConfig.Aspect aspect = config.get(method);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }

        return result;
    }
}
