package com.yc.tomcat.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartTomcat {
	public static void main(String[] args) {
		try {
			new StartTomcat().start();  // 启动服务器
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	@SuppressWarnings("resource")
	public void start() throws IOException {
		// 获取端口
		int port = Integer.parseInt(ReadConfig.getInstance().getProperty("port"));
		
		// 启动服务 —> ServerSocket
		ServerSocket ssk = new ServerSocket(port);
		
		System.out.println("服务器启动成功，占用端口：" + port);
		
		new ParseUrlPatter(); // 扫描webapps下面的所有项目下面的WEB-INF/web.xml文件，解析获取映射路径
		
		new ParseWebXml(); // 解析web.xml文件，获取文件后缀对应的类型
		
		// 创建一个线程池，用来监听请求，初始连接大小为20
		ExecutorService serviceThread = Executors.newFixedThreadPool(20);
		
		Socket sk = null;
		
		while (true) {
			sk = ssk.accept();
			
			// 交给线程池处理
			serviceThread.submit(new ServerService(sk));
		}
	}
}
