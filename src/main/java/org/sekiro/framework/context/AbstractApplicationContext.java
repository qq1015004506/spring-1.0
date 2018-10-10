package org.sekiro.framework.context;

/**
 * author      : quzhiyu
 * date        : 2018/10/6 16:53
 * email       : 1015004506@qq.com
 * description :
 */

public abstract class AbstractApplicationContext {
    //提供给子类重写
    protected void onRefresh() {

    }

    protected abstract void refreshBeanFactory();
}
