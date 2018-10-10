package org.sekiro.framework.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * author      : quzhiyu
 * date        : 2018/10/6 19:16
 * email       : 1015004506@qq.com
 * description :
 */

public class AopProxyUtils {
    public static Object getTargetObject(Object proxy) throws Exception {
        if(!isAopProxy(proxy))
            return proxy;
        return getProxyTargetObject(proxy);
    }

    private static boolean isAopProxy(Object object) {
        return Proxy.isProxyClass(object.getClass());
    }

    private static Object getProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field target = aopProxy.getClass().getDeclaredField("target");
        target.setAccessible(true);
        return target.get(aopProxy);
    }

}
