package com.konka.eplay.interfaces;

/**
 * 
 * Created on: 2015年3月4日
 * 
 * @brief 视频播放器对象的操作
 * @author Eric Fung
 * @date Latest modified on: 2015年3月4日
 * @version V1.0.00
 * 
 */
public interface IMoviePlayer {
	/**
	 * @brief 快进
	 */
	public void ff();

	/**
	 * @brief 快退
	 */
	public void fb();

	/**
	 * @brief 切换字幕
	 */
	public void switchSubtitle(int which);

	/**
	 * @brief 打开字幕
	 */
	public void turnOnSubtitle();

	/**
	 * @brief 关闭字幕
	 */
	public void turnOffSubtitle();

	/**
	 * @brief 设置字幕资源
	 * @param uri
	 */
	public void setSubtitleDataSource(String uri);

	/**
	 * @brief 获取字幕索引
	 * @return 当前字幕对应的索引值
	 */
	public int getSubtitle();

	/**
	 * @brief 切换音轨
	 */
	public void switchTrack(int which);

	/**
	 * @brief 获取音轨索引
	 * @return 当前音轨对应的索引值
	 */
	public int getTrack();

	/**
	 * @brief 切换屏显模式
	 * @param playerType
	 *            TODO
	 * @param mode
	 *            TODO
	 */
	public void switchScreenDisplayMode(int playerType, int mode);

	/**
	 * @brief 调节背光
	 */
	public void switchBrightness();

	/**
	 * @brief 重置播放器
	 */
	public void reset();

	/**
	 * @brief 开始播放
	 */
	public void start();

	/**
	 * @brief 跳转到
	 * @param second
	 *            指定时间点的秒数
	 */
	public void seek(int second);

	/**
	 * @brief 是否正在播放
	 * @return 正在播放返回true，否则返回false
	 */
	public boolean isPlaying();

	/**
	 * @brief 是否准备好
	 * @return 已经准备好返回true，否则返回false
	 */
	public boolean isPrepared();

	/**
	 * @brief 初始化播放器
	 */
	public void initialize();
}
