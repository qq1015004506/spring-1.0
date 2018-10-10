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
	
	@RequestMapping("/index.html")
	public ModelAndView query(@RequestParam("name") String name){
		Map<String,Object> model = new HashMap<>();
		model.put("name", name);
		return new ModelAndView("index.html",model);
	}
	
}
