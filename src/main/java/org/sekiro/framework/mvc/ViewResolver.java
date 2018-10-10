package org.sekiro.framework.mvc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author      : quzhiyu
 * date        : 2018/10/5 21:51
 * email       : 1015004506@qq.com
 * description :
 *     1.将一个静态文件变成一个动态文件 html -> jsp
 *     2.根据用户传送的参数不同，显示不同的界面
 *     3.最终输出字符串，交给response输出
 */

public class ViewResolver {

    private String viewName;
    private File templateFile;

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public ViewResolver(String viewName, File templateFile) {
        this.viewName = viewName;
        this.templateFile = templateFile;
    }

    public String viewResolver(ModelAndView mv) throws Exception {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.templateFile,"r");
        String line = null;
        while (null != (line = ra.readLine())) {
            Matcher matcher = matcher(line);
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String paramName = matcher.group(i);
                    Object paramValue = mv.getModel().get(paramName);
                    if(paramValue == null)
                        continue;
                    line = line.replaceAll("¥\\{"+paramName+"\\}",paramValue.toString());
                }
            }

            sb.append(line);
        }
        return sb.toString();
    }

    private Matcher matcher(String line) {
        Pattern pattern = Pattern.compile("¥\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
        return pattern.matcher(line);
    }
}
