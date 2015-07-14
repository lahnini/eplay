package com.konka.eplay.interfaces;

/**
 * 
 * Created on: 2015年2月28日
 * 
 * @brief 浏览状态的对象的操作
 * @author Eric Fung
 * @date Latest modified on: 2015年2月28日
 * @version V1.0.00
 * 
 */
public interface IBrowseOperation {
	/**
	 * @brief 播放/打开
	 */
	public void open();

	/**
	 * @brief 删除
	 */
	public void delete();

	/**
	 * @brief 获取属性信息
	 */
	public void getAttributes();

	/**
	 * @brief 获取详细信息
	 */
	public void getDetail();
}
