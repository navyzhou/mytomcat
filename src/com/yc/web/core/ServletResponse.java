package com.yc.web.core;

import java.io.IOException;
import java.io.PrintWriter;

public interface ServletResponse {
	public PrintWriter getWriter() throws IOException;
	
	/**
	 * 重定向方法
	 * @param url
	 */
	public void sendRedirect(String url);
}
