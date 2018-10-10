package org.sekiro.framework.beans;

import org.sekiro.framework.aop.AopConfig;
import org.sekiro.framework.aop.AopProxy;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 11:00
 * email       : 1015004506@qq.com
 * description :
 */

public class BeanWrapper {


    private AopProxy aopProxy = new AopProxy();

    //观察者模式
    //1，支持事件响应
    private BeanPostProcessor postProcessor;

    public BeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(BeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    private Object wrapperInstance;
    //原始的通过反射new出来的类
    private Object originalInstance;

    public BeanWrapper(Object instance) {
        //
        this.wrapperInstance = aopProxy.getProxy(instance);
        this.originalInstance = instance;
    }
    public Object getWrapperInstance(){
        return this.wrapperInstance;
    }

    //返回代理以后的Class
    public Class<?> getWrappedClass(){
        return this.wrapperInstance.getClass();
    }

    public void setAopConfig(AopConfig aopConfig){
        aopProxy.setConfig(aopConfig);
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }
}
