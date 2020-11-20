package com.yc.nio.shop.servlet;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

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
		String name = request.getParameter("name");
		String pwd = request.getParameter("pwd");
		
		SocketChannel channel = null;
		
		try {
			channel = response.getWriter();
			channel.write(Charset.forName("UTF-8").encode("name = " + name + " pwd = " + pwd));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
