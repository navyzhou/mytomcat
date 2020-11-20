package com.yc.nio.web.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.yc.nio.tomcat.core.ParseWebXml;
import com.yc.util.StringUtil;

/**
 * 处理响应
 * company 源辰信息
 * @author navy
 * @date 2020年11月5日
 * Email haijunzhou@hnit.edu.cn
 */
public class HttpServletResponse implements ServletResponse{
	private String basePath = ConstantInfo.BASE_PATH; // 获取服务器的基址路径
	private String projectName;
	private SocketChannel channel;
	
	public HttpServletResponse(String projectName, SocketChannel channel) {
		this.channel = channel;
		this.projectName = projectName;
	}

	@Override
	public SocketChannel getWriter() throws IOException {
		String responseHeader = "HTTP/1.1 200 OK\r\nContent-Type:text/html;charset=utf-8\r\n\r\n";
		channel.write(ByteBuffer.wrap(responseHeader.getBytes()));
		return channel;
	}

	@Override
	public void sendRedirect(String url) {
		// /snacknet   /snacknet/   /snacknet/index.html  /snacknet/loginServle访问Servlet
		if (StringUtil.checkNull(url)) {
			error404(url);
			return;
		}
		
		if (!url.startsWith(projectName)) { // 如果不是以项目名开头，说明是动态处理里面的跳转，采用的是相对路径方式
			url = projectName + "/" + url;
		}
		
		if (url.startsWith("/") && url.indexOf("/") == url.lastIndexOf("/")) { // 说明请求方式是  /snacknet
			send302(url);
		} else { // /snacknet/
			if (url.endsWith("/")) { // 说明没有接具体的资源
				String defaultPath = ConstantInfo.DEFAULT_RESOURCE; // 获取默认资源路径
				
				// 读取默认资源
				File fl = new File(basePath, url.substring(1).replace("/", "\\") + defaultPath);
				if (!fl.exists()) {
					error404(url);
					return;
				}
				
				// 如果存在，则读取资源，返回
				send200(readFile(fl), defaultPath.substring(defaultPath.lastIndexOf(".") + 1).toLowerCase());
				return;
			} else {
				File fl = new File(basePath, url.substring(1).replace("/", "\\"));
				if (!fl.exists()) {
					error404(url);
					return;
				}
				send200(readFile(fl), url.substring(url.lastIndexOf(".") + 1).toLowerCase());
			}
		}
	}
	
	private void send302(String url) {
		try {
			String responseHeader = "HTTP/1.1 302 Moved Temporarily\r\nContent-Type:text/html;charset=utf-8\r\nLocation:" + url + "/\r\n";
			channel.write(ByteBuffer.wrap(responseHeader.getBytes()));
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
	
	/**
	 * 读取指定文件到字节数组中
	 * @param fl
	 * @return
	 */
	private byte[] readFile(File fl) {
		byte[] bt = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fl);
			bt = new byte[fis.available()];
			fis.read(bt);
			// 处理响应
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bt;
	}

	private void send200(byte[] bt, String extension) {
		String contentType = "text/html;charset=utf-8";
		String type = ParseWebXml.getContentType(extension);
		
		if (!StringUtil.checkNull(type)) {
			contentType = type;
		}
		
		try {
			String responseHeader = "HTTP/1.1 200 OK\r\nContent-Type:" + contentType + "\r\n";
			responseHeader += "Content-Length:" + bt.length + "\r\n\r\n";
			channel.write(ByteBuffer.wrap(responseHeader.getBytes()));
			channel.write(ByteBuffer.wrap(bt));
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

	/**
	 * 404响应
	 * 注意此时响应的数据流必须满足响应头协议
	 * @param url
	 */
	private void error404(String url) {
		try {
			String data = "<h1>HTTP Status 404 - " + url + "</h1>";
			String responseHeader = "HTTP/1.1 404 File Not Found\r\nContent-Type:text/html;charset=utf-8\r\n";
			responseHeader += "Content-Length:" + data.length() + "\r\n\r\n" + data;
			channel.write(ByteBuffer.wrap(responseHeader.getBytes()));
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
