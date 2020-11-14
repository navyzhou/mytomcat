package com.yc.web.core;

public interface ServletRequest {
	/**
	 * 解析请求的方法
	 */
	public void parse();
	
	/**
	 * 获取请求参数的方法
	 * @param key
	 * @return
	 */
	public String getParameter(String key);
	
	/**
	 * 获取请求方法的方法
	 * @return
	 */
	public String getMethod();
	
	/**
	 * 获取请求地址的方法
	 * @return
	 */
	public String getUrl();
}
