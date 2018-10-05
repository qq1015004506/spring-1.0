package org.sekiro.framework.beans;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 15:55
 * email       : 1015004506@qq.com
 * description : 用于事件监听
 */

public class BeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }
    public Object postProcessAfterInitialization(Object bean, String beanName){
        return bean;
    }

}
