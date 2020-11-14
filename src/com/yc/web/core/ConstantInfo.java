package com.yc.web.core;

import com.yc.tomcat.core.ReadConfig;

public class ConstantInfo {
	public static final String REQUEST_METHOD_GET = "GET";
	public static final String REQUEST_METHOD_POST = "POST";
	public static final String BASE_PATH = ReadConfig.getInstance().getProperty("path"); // 基址路径
	public static final String DEFAULT_RESOURCE = ReadConfig.getInstance().getProperty("default"); // 默认资源 
}
