package org.sekiro.demo.service.impl;

import org.sekiro.demo.service.IQueryService;
import org.sekiro.framework.annotation.Service;

import java.text.SimpleDateFormat;
import java.util.Date;


@Service
public class QueryService implements IQueryService {

	/**
	 * 查询
	 */
	public String query(String name) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
		return json;
	}

}
