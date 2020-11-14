package com.yc.tomcat.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

import com.yc.util.StringUtil;
import com.yc.web.core.ConstantInfo;
import com.yc.web.core.HttpServletRequest;
import com.yc.web.core.HttpServletResponse;
import com.yc.web.core.Servlet;
import com.yc.web.core.ServletRequest;
import com.yc.web.core.ServletResponse;

/**
 * 提供服务的方法
 * company 源辰信息
 * @author navy
 * @date 2020年11月1日
 * Email haijunzhou@hnit.edu.cn
 */
public class ServerService implements Runnable{
	private Socket sk;
	private InputStream is;
	private OutputStream os;

	public ServerService(Socket sk) {
		this.sk = sk;
	}

	@Override
	public void run() { // GET /xxx  HTTP/1.1
		try {
			this.is = sk.getInputStream(); // 相当于是request
			this.os = sk.getOutputStream(); // 相当于是response
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// 处理请求 -> 解析请求头协议   GET /index.html  HTTP/1.1
		ServletRequest request = new HttpServletRequest(is);

		// 获取请求的地址 -> 这个地址是一个静态资源地址，另外也可能是动态资源地址如servlet
		String url = request.getUrl();

		// 如果请求地址中含有中文，此时会被转成urlencoding编码，那么这个时候就无法正确的获取这个资源
		try {
			url = URLDecoder.decode(url, "utf-8"); // 处理中文路径  
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		// url = /snacknet/index.html
		String urlTemp = url.substring(1); // 去掉最前面的/
		String projectName = (urlTemp.contains("/") ? urlTemp.substring(0, urlTemp.indexOf("/")) : urlTemp); //  /snacknet

		// 交给响应处理
		ServletResponse response = new HttpServletResponse("/" + projectName, os);

		// 这个地址是静态资源地址还是动态资源地址 -> 如何判断是静态资源地址还是动态资源地址呢?
		String clazz = ParseUrlPatter.getClass(url);
		if ( StringUtil.checkNull(clazz) ) { // 如果根据这个映射地址能取不到处理类，则当成静态态资源处理
			response.sendRedirect(url);
			return;
		}

		// 否则当成动态资源处理
		// 根据类路径动态的加载这个类  -> 所以我们需要一个类加载器
		URLClassLoader loader = null;
		URL classPath = null; // 要加载的这个类的完整绝对路径

		// 第一个参数要加载的资源类型
		try {
			classPath = new URL("file", null, ConstantInfo.BASE_PATH + "\\" + projectName + "\\bin");

			// 创建一个类加载器，告诉他我接下来要加载的类，在哪个路径里面找
			loader = new URLClassLoader(new URL[] {classPath});

			Class<?> cls = loader.loadClass(clazz);

			Servlet servlet = (Servlet) cls.newInstance(); // 得到这个类的一个实例化对象

			// 将这个请求交给service()方法处理
			servlet.service(request, response);
		} catch (Exception e) {
			send500(e);
			e.printStackTrace();
		}
	}

	private void send500(Exception e) {
		try {
			String responseHeader = "HTTP/1.1 500 Error\r\n\r\n" + e.getMessage();
			os.write(responseHeader.getBytes());
			os.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
