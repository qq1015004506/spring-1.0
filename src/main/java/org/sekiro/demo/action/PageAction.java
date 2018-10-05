package org.sekiro.demo.action;

import org.sekiro.demo.service.IQueryService;
import org.sekiro.framework.annotation.Autowired;
import org.sekiro.framework.annotation.Controller;
import org.sekiro.framework.annotation.RequestMapping;
import org.sekiro.framework.annotation.RequestParam;
import org.sekiro.framework.mvc.ModelAndView;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/")
public class PageAction {

	@Autowired
	IQueryService queryService;
	
	@RequestMapping("/first.html")
	public ModelAndView query(@RequestParam("teacher") String teacher){
		String result = queryService.query(teacher);
		Map<String,Object> model = new HashMap<String,Object>();
		model.put("teacher", teacher);
		model.put("data", result);
		model.put("token", "123456");
		return new ModelAndView("first.html",model);
	}
	
}
