package com.konka.eplay.interfaces;

import android.graphics.Bitmap;

/**
 * 
 * Created on: 2015年2月13日
 * 
 * @brief 描述浏览状态下的对象特征
 * @author Eric Fung
 * @date Latest modified on: 2015年2月13日
 * @version V1.0.00
 * 
 */
public interface IBrowseObject {
	/**
	 * @brief 获取缩略图
	 * @return
	 */
	public Bitmap getThumb();

	/**
	 * @brief 获取展示名称
	 * @return
	 */
	public String getShownName();

	/**
	 * @brief 执行操作
	 * @param cmd
	 */
	public void operate(String cmd);
}
