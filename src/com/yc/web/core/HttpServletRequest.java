package com.yc.web.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yc.util.StringUtil;

public class HttpServletRequest implements ServletRequest {
	private String method; // 请求方式
	private Map<String, String> parameter = new HashMap<String, String>(); // 请求中的参数
	private String url; // 请求的资源地址
	private String protocalVersion; // 协议版本
	private InputStream is; // 请求数据流
	private BufferedReader reader;
	
	public HttpServletRequest(InputStream is) {
		this.is = is;
		parse();
	}

	/**
	 * 解析请求
	 */
	@Override
	public void parse() {
		try {
			reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			List<String> header = new ArrayList<String>();
			// 读取所有的请求头协议数据
			while ( (line = reader.readLine()) != null && !"".equals(line)) {
				header.add(line);
			}
			
			//header.forEach(System.out::println);
			
			parseFirstLine(header.get(0)); // 解析第一行，即起始行
			
			parseParamter(header);
		} catch (IOException e) {
			e.printStackTrace();
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
	private void parseParamter(List<String> header) {
		// TODO 解析和获取请求参数
		String str = header.get(0).split(" ")[1]; // 获取请求地址  GET /fresh/login?account=yc HTTp/1.1
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
				char[] ch = new char[1024 * 10];
				int count = 0, total = 0;  // count是每次读到的大小，total是已经多了多少
				StringBuffer sbf = new StringBuffer(1024 * 10);
				while ( (count = reader.read(ch) ) > 0) {
					sbf.append(ch, 0, count);
					total += count;
					if (total >= len) { // 如果已读取的数据已经大于等于要接收到的数据，则退出
						break;
					}
				}
				
				str = URLDecoder.decode(sbf.toString(), "utf-8");
				String[] params = str.split("&");
				String[] temp = null;
				
				for (String param : params) {
					temp = param.split("=");
					this.parameter.put(temp[0].trim(), temp[1].trim());
				}
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
