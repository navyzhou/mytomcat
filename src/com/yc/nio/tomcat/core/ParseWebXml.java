package com.yc.nio.tomcat.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 解析web.xml文件，获取资源后缀对应的类型
 * company 源辰信息
 * @author navy
 * @date 2020年11月5日
 * Email haijunzhou@hnit.edu.cn
 */
public class ParseWebXml {
	private static Map<String, String> map = new HashMap<String, String>();
	
	public ParseWebXml() {
		init();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		SAXReader reader = new SAXReader();
		Document doc = null;
		
		try {
			doc = reader.read(this.getClass().getClassLoader().getResourceAsStream("web.xml"));
			List<Element> mines = doc.selectNodes("//mime-mapping");
			for (Element el : mines) {
				map.put(el.selectSingleNode("extension").getText().trim(), el.selectSingleNode("mime-type").getText().trim());
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public Map<String, String> getMap() {
		return map;
	}
	
	public static String getContentType(String key) {
		return map.getOrDefault(key, null);
	}
}
