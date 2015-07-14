package com.konka.eplay.interfaces;

/**
 * 
 * Created on: 2015年2月13日
 * 
 * @brief 播放器特征
 * @author Eric Fung
 * @date Latest modified on: 2015年2月13日
 * @version V1.0.00
 * 
 */
public abstract class IPlayer {
	/**
	 * @brief 继续播放
	 */
	public abstract void resume();

	/**
	 * @brief 暂停播放
	 */
	public abstract void pause();

	/**
	 * @brief 停止播放
	 * @param force
	 *            true: 表示认为音视频格式有一个不支持时也要停止播放 false: 表示音视频格式只要有一个支持时就不停止播放
	 */
	public abstract void stop(boolean force);

	/**
	 * @brief 重播
	 */
	public abstract void replay();

	/**
	 * @brief 播放上一个
	 */
	public abstract void prev();

	/**
	 * @brief 播放下一个
	 */
	public abstract void next();

	/**
	 * @brief 获取总时长
	 * @return 时长数值
	 */
	public abstract int getDuration();

	/**
	 * @brief 获取当前播放时刻
	 * @return 当前播放时刻的数值
	 */
	public abstract int getCurrent();
}
