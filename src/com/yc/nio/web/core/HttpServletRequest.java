package com.yc.nio.web.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.yc.util.StringUtil;

public class HttpServletRequest implements ServletRequest {
	private String method; // 请求方式
	private Map<String, String> parameter = new HashMap<String, String>(); // 请求中的参数
	private String url; // 请求的资源地址
	private String protocalVersion; // 协议版本
	private static final int BUFFERSIZE = 1024; // 缓冲区大小  
	private SelectionKey key;
	private SocketChannel channel;

	public HttpServletRequest(SelectionKey key) throws IOException {
		this.key = key;
		parse();
	}

	/**
	 * 处理请求头数据
	 * @throws IOException 
	 */
	public void parse() throws IOException {
		// 1、先获得与客户端通信的信道  
		channel = (SocketChannel) key.channel();  

		// 2、得到附加对象，如果为空则自己创建一个
		ByteBuffer buffer = (ByteBuffer) key.attachment(); //获取当前的附加对象。当前已附加到此键的对象，如果没有附加对象，则返回 null
		if (buffer == null) {
			buffer = ByteBuffer.allocate(BUFFERSIZE);
		}

		// 清空缓冲区 将位置设置为 0，将限制设置为容量，并丢弃标记。
		buffer.clear();  

		// 3、循环读取客户端发过来的信息
		String line = "";
		while ( channel.read(buffer) > 0 ) {
			// 将缓冲区准备为数据传出状态 ，即将写模式转换成读模式
			buffer.flip();

			// 将字节转化为为UTF-8的字符串  
			// line += new String(buffer.array(), 0, buffer.remaining(), "UTF-8");
			line += Charset.forName("UTF-8").decode(buffer).toString();  
		}

		// 4、将channel再次注册到selector上，继续监听它的可读事件，等待下一个客户端发信息过来
		channel.register(key.selector(), SelectionKey.OP_READ);

		// 5、处理客户端数据
		if (line.length() > 0) {
			String[] lines = line.split("\r\n");
			parseFirstLine(lines[0]); // 解析第一行，即起始行
			parseParamter(lines);
		}  
	}

	/**
	 * 解析起始行
	 * @param string
	 */
	private void parseFirstLine(String str) {
		if (StringUtil.checkNull(str)) {
			return;
		}
		String[] arrs = str.split(" "); // GET /snacknet/index.html HTTP/1.1
		this.method = arrs[0];

		// 检查请求地址中有没有带参数
		if (arrs[1].contains("?")) { // 说明请求地址中有参数
			this.url = arrs[1].substring(0, arrs[1].indexOf("?"));
		} else {
			this.url = arrs[1];
		}
		this.protocalVersion = arrs[2];
	}

	/**
	 * 解析请求参数
	 * @param header
	 */
	private void parseParamter(String[] header) {
		// TODO 解析和获取请求参数
		String str = header[0].split(" ")[1]; // 获取请求地址  GET /fresh/login?account=yc HTTp/1.1
		if (str.contains("?")) { // 说明地址栏中有参数
			str = str.substring(str.indexOf("?") + 1);
			String[] params = str.split("&");
			String[] temp = null;

			for (String param : params) {
				temp = param.split("=");
				this.parameter.put(temp[0].trim(), temp[1].trim());
			}
		}

		if (ConstantInfo.REQUEST_METHOD_POST.equals(this.method)) { // 说明是post请求
			// 先要获取到请求头协议中的Content-Length
			int len = 0;

			for (String head : header) {
				if (head.contains("Content-Length: ")) { // 说明找到协议行  Content-Length: 200
					len = Integer.parseInt(head.substring(head.indexOf(":") + 1).trim());
					break;
				}
			}

			if (len <= 0) { // 说明post请求中没有带参数
				return;
			}

			// 如果有带参数，则我们需要先获取Content-Type，判断数据格式，我这里就不判断了，都当成字符串处理
			try {
				ByteBuffer buffer = ByteBuffer.allocate(BUFFERSIZE);

				String line = "";
				int total = 0, count = 0;
				while ( (count = channel.read(buffer)) > 0 ) {
					// 将缓冲区准备为数据传出状态 ，即将写模式转换成读模式
					buffer.flip();

					// 将字节转化为为UTF-8的字符串  
					// line += new String(buffer.array(), 0, buffer.remaining(), "UTF-8");
					line += Charset.forName("UTF-8").decode(buffer).toString(); 
					total += count;
					if (total >= len) { // 如果已读取的数据已经大于等于要接收到的数据，则退出
						break;
					}
				}
				
				String[] params = line.split("&");
				String[] temp = null;

				for (String param : params) {
					temp = param.split("=");
					this.parameter.put(temp[0].trim(), temp[1].trim());
				}
				
				// 4、将channel再次注册到selector上，继续监听它的可读事件，等待下一个客户端发信息过来
				channel.register(key.selector(), SelectionKey.OP_READ);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getParameter(String key) {
		return this.parameter.getOrDefault(key, null);
	}

	@Override
	public String getMethod() {
		return this.method;
	}

	@Override
	public String getUrl() {
		return this.url;
	}

	public String getProtocalVersion() {
		return protocalVersion;
	}
}
