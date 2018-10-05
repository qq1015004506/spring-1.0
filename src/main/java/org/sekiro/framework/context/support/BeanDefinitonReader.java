package org.sekiro.framework.context.support;

import org.sekiro.framework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 10:58
 * email       : 1015004506@qq.com
 * description : 对配置文件进行查找，读取，解析
 */

public class BeanDefinitonReader {

    private Properties config = new Properties();
    private List<String> registyBeanClasses = new ArrayList<>();


    private final String SCAN_PACKAGE="scanPackage";

    public BeanDefinitonReader(String... locations) {

        InputStream input = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));
        try {
            config.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    public List<String> loadBeanDefinitions(){
        return registyBeanClasses;
    }

    public Properties getConfig() {
        return this.config;
    }


    /*
     * @Author quzhiyu
     * @Description 递归扫描所有相关联的class, 并且保存到一个list中
     * @Date 11:15 2018/10/5
     * @Param [packageName]
     * @return void
     **/
    private void doScanner(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.","/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else {
                registyBeanClasses.add(packageName+"."+file.getName().replace(".class",""));
            }
        }
    }

    
    /*
     * @Author quzhiyu
     * @Description
     *     每注册一个className,就返回一个BeanDefinition
     *     对配置信息进行包装
     * @Date 11:16 2018/10/5
     * @Param [className]
     * @return org.sekiro.framework.beans.BeanDefinition
     **/
    public BeanDefinition registerBean(String className){
        if(this.registyBeanClasses.contains(className)) {
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".")+1)));
            return beanDefinition;
        }
        return null;
    }

    private String lowerFirstCase(String className) {
        char[] chars = className.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
