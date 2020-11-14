package com.yc.web.core;

public interface Servlet {
	public void init();
	
	public void service(ServletRequest request, ServletResponse response);
	
	public void doGet(ServletRequest resquest, ServletResponse response);
	
	public void doPost(ServletRequest request, ServletResponse response);
}
