package com.yc.nio.fresh.servlet;

import com.yc.nio.web.core.HttpServlet;
import com.yc.nio.web.core.ServletRequest;
import com.yc.nio.web.core.ServletResponse;

public class LoginServlet extends HttpServlet{
	@Override
	public void doGet(ServletRequest request, ServletResponse response) {
		doPost(request, response);
	}
	
	@Override
	public void doPost(ServletRequest request, ServletResponse response) { 
		System.out.println("nickName = " + request.getParameter("nickName"));
		System.out.println("pwd = " + request.getParameter("pwd"));
		response.sendRedirect("login.html");
	}
}
