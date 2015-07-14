package com.konka.eplay.interfaces;

/**
 * 
 * Created on: 2015年2月13日
 * 
 * @brief 图片播放器对象的操作
 * @author Eric Fung
 * @date Latest modified on: 2015年2月13日
 * @version V1.0.00
 * 
 */
public interface IPhotoPlayer {
	/**
	 * @brief 放大
	 */
	public void zoomIn();

	/**
	 * @brief 缩小
	 */
	public void zoomOut();

	/**
	 * @brief 旋转
	 */
	public void rotate();
}
