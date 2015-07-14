package com.konka.eplay.interfaces;

/**
 * 
 * Created on: 2015年2月13日
 * 
 * @brief 音频播放器对象的操作
 * @author Eric Fung
 * @date Latest modified on: 2015年2月13日
 * @version V1.0.00
 * 
 */
public interface IMusicPlayer {
	/**
	 * @brief 快进
	 */
	public void ff();

	/**
	 * @brief 快退
	 */
	public void fb();

	/**
	 * @brief 搜索歌词
	 */
	public void searchLrc();

	/**
	 * @brief 下载歌词
	 */
	public void downloadLrc();

	/**
	 * @brief 切换音效
	 */
	public void switchEffective();

	/**
	 * @brief 显示歌词
	 */
	public void displayLrc();
}
