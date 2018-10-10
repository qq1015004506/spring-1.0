package org.sekiro.framework.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * author      : quzhiyu
 * date        : 2018/10/6 17:42
 * email       : 1015004506@qq.com
 * description :
 *     是对application中的expression的封装
 *     用自己实现的业务逻辑去增强目标代理对象的方法
 *     通过配置文件得知哪些类的哪些方法需要增强
 *     增强的内容是什么
 *     对配置文件中所体现的内容进行封装
 */

public class AopConfig {
    //以目标对象需要增强的Method作为key，需要增强的代码内容作为value
    private Map<Method,Aspect> points = new HashMap<>();
    public void put(Method target,Object aspect,Method[] points){
        this.points.put(target,new Aspect(aspect,points));
    }

    public Aspect get(Method method) {
        return this.points.get(method);
    }

    public Boolean contains(Method method) {
        return this.points.containsKey(method);
    }

    public class Aspect {
        private Object aspect;//包含增强内容的对象
        private Method[] points;//增强方法，before after
        public Aspect(Object aspect,Method[] points) {
            this.aspect = aspect;
            this.points = points;
        }

        public Object getAspect() {
            return aspect;
        }

        public Method[] getPoints() {
            return points;
        }
    }
}
