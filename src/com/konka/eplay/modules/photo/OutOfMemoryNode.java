package com.konka.eplay.modules.photo;

/**
 * 
 * Created on: 2015-6-5
 * 
 * @brief 用于记录哪个文件路径发生了oom
 * @author mcsheng
 * @date Latest modified on: 2015-6-5
 * @version V1.0.00
 */
public class OutOfMemoryNode {
	
	private static String mFilePath;
	
	private static Boolean mIsOom = false;
	
	public static synchronized void setFilePath(String filePath) {
		mFilePath = filePath;
	}
	
	public static synchronized String getFilePath(String filePath) {
		return mFilePath;
	}
	
	public static synchronized void setIsOom(Boolean isOom) {
		mIsOom = isOom;
	}
	
	public static synchronized Boolean getIsOom() {
		return mIsOom;
	}
	
}