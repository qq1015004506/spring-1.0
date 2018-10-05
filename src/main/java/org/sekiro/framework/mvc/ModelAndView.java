package org.sekiro.framework.mvc;

import java.util.Map;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 21:41
 * email       : 1015004506@qq.com
 * description :
 */

public class ModelAndView {
    private String viewName;
    private Map<String,?> model;

    public ModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }

}
