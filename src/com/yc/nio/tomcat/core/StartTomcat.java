package com.yc.nio.tomcat.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class StartTomcat {
	private static final int TIMEOUT = 3000; // 超时时间，单位毫秒 
	
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

		// 1、创建Selector
		Selector selector = Selector.open(); // 创建选择器

		// 2、通过ServerSocketChannel创建channel
		ServerSocketChannel channel = ServerSocketChannel.open(); // 打开通道

		// 3、为channel并绑定监听端口
		channel.bind(new InetSocketAddress(port));

		// 4、将channel设置为非阻塞模式
		channel.configureBlocking(false); // 设置为非阻塞模式  

		/*
		 * 5、将channel注册到Selector上，监听连接事件
		 * SelectionKey.OP_ACCEPT:用于套接字接收连接操作的就绪
		 * SelectionKey.OP_CONNECT:用于套接字连接操作的就绪
		 * SelectionKey.OP_READ:用于读取操作的就绪
		 * SelectionKey.OP_WRITE:用于写入操作的就绪
		 */

		channel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("服务器启动成功，占用端口：" + port);

		new ParseUrlPatter(); // 扫描webapps下面的所有项目下面的WEB-INF/web.xml文件，解析获取映射路径

		new ParseWebXml(); // 解析web.xml文件，获取文件后缀对应的类型

		// 6、循环调用Selector的select方法，检测就绪情况
		while(true) {
			// 等待某信道就绪(或超时)  selector.select() 返回就绪事件的个数
			if (selector.select(TIMEOUT) == 0) {// 如果没有就绪的信道时
				System.out.println("独自等待...");
				continue;  
			}
			// 7、调用selectedKeys方法获取就绪channel集合
			// 如果有已经准备好的信道，则获取所有已经准备好的信道
			Set<SelectionKey> setKeys = selector.selectedKeys();
			// Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();  

			// 迭代取出每一个信道处理
			Iterator<SelectionKey> iterator = setKeys.iterator(); 
			SelectionKey key = null;
			while ( iterator.hasNext() ) { // 如果有还有准备好的信道，则进行如下处理
				key = iterator.next();

				// 8、判断就绪事件种类，调用业务处理方法
				try {  
					if (key.isAcceptable()) { // 如果是接入事件，即有连接上来了
						acceptHandler(key); // 有客户端连接请求时  
					} 
					
					if (key.isReadable()) {// 如果是可读事件，即：有数据发送过来需要读取  
						new ServerService(key);
					}  
				} catch (IOException e) {  
					// 出现IO异常（如客户端断开连接）时移除这个信道
					iterator.remove();  
					continue;  
				}  
				// 移除处理过的信道
				iterator.remove();  
			}  
		}
	}
	
	/**
	 * 接入事件处理方法
	 * @throws IOException 
	 */
	private void acceptHandler(SelectionKey key) throws IOException {
		// 1、 创建一个SocketChannel，用来维持与客户端的连接
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel(); // 获取与这个客户端通信的信道
		SocketChannel socketChannel = serverChannel.accept(); // 获取客户端连接

		// 2、将socketChannel设置为非阻塞方式
		socketChannel.configureBlocking(false);

		// 3、将这个channel注册到selector上，监听可读事件，这样当客户端有信息发过来时，就可以被服务器监听到，然后通知我们处理
		//  分配一个新的字节缓冲区
		socketChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(1024));
		// 4、给客户端一个响应   以UTF-8格式给客服端一个响应
	}
}
