package com.yc.nio.tomcat.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.yc.web.core.ConstantInfo;

/**
 * 解析获取映射路径以及对应的处理类路径
 * company 源辰信息
 * @author navy
 * @date 2020年11月8日
 * Email haijunzhou@hnit.edu.cn
 */
public class ParseUrlPatter {
	private String basePath = ConstantInfo.BASE_PATH; // 服务器存放项目的目录
	
	// 存放解析后信息，一个映射路径对应一个处理类，不过这个映射路径里面必须包含项目名
	private static Map<String, String> urlPattern = new HashMap<String,  String>(); 
	
	public ParseUrlPatter() {
		parse();
	}

	private void parse() {
		// 获取服务器路径下的所有项目
		File[] files = new File(basePath).listFiles();
		
		// 说明此时服务器中没有项目，则不管
		if (files == null || files.length <= 0) {
			return;
		}
		
		String projectName = null; // 此时项目名其实就是webapps下的文件夹的名字
		File webFile = null;
		// 循环解析每个项目的web.xml文件获取映射信息
		for (File fl : files) {
			if (!fl.isDirectory()) { // 如果当前不是文件夹，则不管，进行下一次循环
				continue;
			}
			
			projectName = fl.getName();
			
			// 获取这个项目项目的WEB-INF/web.xml文件
			webFile = new File(fl, "WEB-INF/web.xml");
			if (!webFile.exists() || !webFile.isFile()) {
				continue;
			}
			
			parseXml(projectName, webFile);
		}
	}

	@SuppressWarnings("unchecked")
	private void parseXml(String projectName, File webFile) {
		SAXReader reader = new SAXReader();
		Document doc = null;
		
		try {
			doc = reader.read(webFile);
			List<Element> servletList = doc.selectNodes("//servlet");
			
			for (Element servlet : servletList) {
				urlPattern.put("/" + projectName + servlet.selectSingleNode("url-pattern").getText().trim(), servlet.selectSingleNode("servlet-class").getText().trim());
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public static String getClass(String url) {
		return urlPattern.getOrDefault(url, null);
	}
	
	public Map<String, String> getUrlPattern() {
		return urlPattern;
	}
}
