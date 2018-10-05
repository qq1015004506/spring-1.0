package org.sekiro.framework.context;

import org.sekiro.framework.annotation.Autowired;
import org.sekiro.framework.annotation.Controller;
import org.sekiro.framework.annotation.Service;
import org.sekiro.framework.beans.BeanDefinition;
import org.sekiro.framework.beans.BeanPostProcessor;
import org.sekiro.framework.beans.BeanWrapper;
import org.sekiro.framework.context.support.BeanDefinitonReader;
import org.sekiro.framework.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 10:50
 * email       : 1015004506@qq.com
 * description :
 */

public class ApplicationContext implements BeanFactory {

    private String [] configLocations;

    private BeanDefinitonReader reader;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    //用来保证注册式单例
    private Map<String,Object> beanCacheMap = new ConcurrentHashMap<>();
    //存储所有被代理过的对象
    private Map<String,BeanWrapper> beanWrapperMap = new ConcurrentHashMap<>();
    public void refresh(){

        //定位
        this.reader = new BeanDefinitonReader(configLocations);

        //加载
        List<String> beanDefinitions = this.reader.loadBeanDefinitions();

        //注册
        doRegistry(beanDefinitions);

        //依赖注入(lazy-init = false), 执行依赖注入
        //自动调用getBean方法
        doAutowired();


    }

    //开始执行依赖注入
    private void doAutowired() {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if(!beanDefinitionEntry.getValue().isLazyInit()) {
                getBean(beanName);
            }
        }

    }


    public void populateBean(String beanName,Object instance) {
        Class<?> clazz = instance.getClass();

        if(!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(!field.isAnnotationPresent(Autowired.class))
                continue;
            Autowired autowired = field.getAnnotation(Autowired.class);
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);
            try {
                if(!beanCacheMap.containsKey(autowiredBeanName)) {
                    getBean(autowiredBeanName);
                }
                field.set(instance, this.beanWrapperMap.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    
    /*
     * @Author quzhiyu
     * @Description
     *     将 beanDefinitions 注册到 beanDefinitionMap中
     * @Date 15:09 2018/10/5
     * @Param [beanDefinitions]
     * @return void
     **/
    private void doRegistry(List<String> beanDefinitions) {

        try {
            for (String className : beanDefinitions) {
                /*
                 * beanName被注册到BeanDefinitionMap中时他的key有三种情况
                 * 1.以首字母小写的类名作为key
                 * 2.自定义的key名:
                 *     @Service("myService")
                 *     public class StudentService
                 *     即是以myService作为key而不是以studentService作为key
                 * 3.接口注入
                 *     如果类实现了某个接口，那么也应当以接口名作为key保存到beanDefinitionMap中一份
                 */
                Class<?> beanClass = Class.forName(className);

                //如果是个接口则不进行注册
                if(beanClass.isInterface())
                    continue;

                BeanDefinition beanDefinition = reader.registerBean(className);
                if(beanDefinition != null) {
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
                }

                //实现接口注入
                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    /*
                     * 如果多个实现类只能覆盖
                     * 在spring会报错
                     */
                    this.beanDefinitionMap.put(i.getName(),beanDefinition);
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public ApplicationContext(String... locations){
        this.configLocations = locations;
        this.refresh();
    }

    /*
     * @Author quzhiyu
     * @Description
     *     DI依赖注入，从这里开始
     *     通过读取BeanDefinition中的信息
     *     再通过反射机制创建实例并返回
     *     spring并不会直接返回原始对象
     *     会用一个BeanWrapper进行包装
     *     方便进行扩展增强(aop)
     * @Date 15:30 2018/10/5
     * @Param [name]
     * @return java.lang.Object
     **/
    @Override
    public Object getBean(String beanName) {

        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        String className = beanDefinition.getBeanClassName();
        try{
            Object instance = instantionBean(beanDefinition);

            //生成通知事件
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
            //实例初始化以前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            if(null == instance)
                return null;
            BeanWrapper beanWrapper = new BeanWrapper(instance);
            beanWrapper.setPostProcessor(beanPostProcessor);
            this.beanWrapperMap.put(beanName,beanWrapper);
            //实例初始化以后通知一次
            beanPostProcessor.postProcessAfterInitialization(instance,beanName);

            populateBean(beanName,instance);
            return this.beanWrapperMap.get(beanName).getWrapperInstance();

        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * @Author quzhiyu
     * @Description
     *     通过BeanDefinition创建实例bean
     * @Date 15:42 2018/10/5
     * @Param [beanDefinition]
     * @return java.lang.Object
     **/
    private Object instantionBean(BeanDefinition beanDefinition){
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try{
            //因为根据className才能确定是否有实例
            if(this.beanCacheMap.containsKey(className)){
                instance = this.beanCacheMap.get(className);
            }else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.beanCacheMap.put(className,instance);
            }
            return instance;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

}
