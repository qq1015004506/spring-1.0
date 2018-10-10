package org.sekiro.framework.context;

import org.sekiro.framework.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author      : quzhiyu
 * date        : 2018/10/6 16:57
 * email       : 1015004506@qq.com
 * description :
 */

public class DefaultListableBeanFactory extends AbstractApplicationContext {

    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    @Override
    protected void refreshBeanFactory() {

    }
}
