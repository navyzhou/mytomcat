package com.yc.nio.web.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ServletResponse {
	public SocketChannel getWriter() throws IOException;
	
	/**
	 * 重定向方法
	 * @param url
	 */
	public void sendRedirect(String url);
}
