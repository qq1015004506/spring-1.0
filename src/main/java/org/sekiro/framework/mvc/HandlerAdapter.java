package org.sekiro.framework.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 21:50
 * email       : 1015004506@qq.com
 * description :
 *     根据参数匹配调用的方法
 */

public class HandlerAdapter {

    //为什么要把handler传进来
    //因为handler中包含了controller method url信息
    //根据用户请求的参数信息，跟method中的参数信息进行动态匹配
    //因为controller中任何方法都可以加入req 和 resp
    //但是req resp 不可以new 出来
    //因此handler方法的req resp参数只是为了将其赋值给处理请求方法的参数
    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) {
        return null;
    }
}
