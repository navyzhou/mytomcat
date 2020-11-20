package com.yc.nio.tomcat.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 采用单例模式读取配置文件
 * company 源辰信息
 * @author navy
 * @date 2020年11月1日
 * Email haijunzhou@hnit.edu.cn
 */
public class ReadConfig extends Properties{
	private static final long serialVersionUID = -5456374340137304750L;
	private static ReadConfig instance = new ReadConfig();
	
	private ReadConfig() {
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("web.properties")) {
			load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static ReadConfig getInstance() {
		return instance;
	}
}
