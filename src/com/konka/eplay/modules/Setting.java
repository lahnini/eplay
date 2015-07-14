package com.konka.eplay.modules;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 
 * Created on: 2014-2-12
 * 
 * @brief 设置功能�?
 * @author Li Huiqi
 * @date Lastest modified on: 2014-2-12
 * @version V1.0.0.00
 * 
 */

public class Setting {

	// 设置使用的常�?
	public static final String CONCISE_SETTING = "CONCISESETTING";
	public static final String SLIDE_INTERVAL = "INTERVAL";
	public static final String MUSIC_PLAYMODE = "MUSICPLAYMODE";
	public static final String VIDEO_PLAYMODE = "VIDEOPLAYMODE";
	public static final String VIDEO_SCALEMODE = "VIDEOSCALEMODE";
	// 排序方式
	public static final String ORDER_TYPE = "ORDERTYPE";
	// 升序或�?�降�?
	public static final String ASC_OR_DESC = "ASCORDESC";

	public static final int interval1 = 5000;
	public static final int interval2 = 10000;
	public static final int interval3 = 15000;

	/**
	 * 
	 * @author Li Huiqi
	 * @param context
	 * @brief 获取幻灯片间隔Flag
	 * 
	 */
	public static int getSlideIntervalFlag(Context context) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		int flag = sp.getInt(SLIDE_INTERVAL, 0);
		return flag;
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @return 幻灯片播放时间间�? 单位(�?)
	 */

	public static int getSlideInterval(Context context) {
		int flag = getSlideIntervalFlag(context);
		if (flag == 0) {
			return interval1;
		} else if (flag == 1) {
			return interval2;
		} else if (flag == 2) {
			return interval3;
		}
		return interval1;
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @param flag
	 * @brief 保存幻灯片播放间隔flag
	 */

	public static void saveSlideInterval(Context context, int flag) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		sp.edit().putInt(SLIDE_INTERVAL, flag).commit();
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @param mode
	 * @brief 保存音乐播放模式
	 */

	public static void saveMusicPlayMode(Context context, int mode) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		sp.edit().putInt(MUSIC_PLAYMODE, mode).commit();
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @param mode
	 * @brief 保存视频播放模式
	 */

	public static void saveVideoPlayMode(Context context, int mode) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		sp.edit().putInt(VIDEO_PLAYMODE, mode).commit();
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @param mode
	 * @brief 保存视频屏显模式
	 */

	public static void saveVideoScaleMode(Context context, int mode) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		sp.edit().putInt(VIDEO_SCALEMODE, mode).commit();
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @return mode
	 * @brief 获取音乐播放模式
	 */

	public static int getMusicPlayMode(Context context) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		return sp.getInt(MUSIC_PLAYMODE, 0);
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @return mode
	 * @brief 获取视频播放模式
	 */

	public static int getVieoPlayMode(Context context) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		return sp.getInt(VIDEO_PLAYMODE, 0);
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @return mode
	 * @brief 获取视频屏显模式
	 */
	public static int getVieoScaleMode(Context context) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		return sp.getInt(VIDEO_SCALEMODE, 0);
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @return ordertype
	 * @brief 获取排序类型
	 */

	public static int getOrderType(Context context) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		return sp.getInt(ORDER_TYPE, 0);
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @param ordertype
	 * @brief 保存排序类型
	 */

	public static void saveOrderType(Context context, int mode) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		sp.edit().putInt(ORDER_TYPE, mode).commit();
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @return asc or desc
	 * @brief 获取顺序
	 */

	public static int getOrder(Context context) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		return sp.getInt(ASC_OR_DESC, 0);
	}

	/**
	 * @author Li Huiqi
	 * @param context
	 * @param asc
	 *            or desc
	 * @brief 保存顺序
	 */

	public static void saveOrder(Context context, int mode) {
		SharedPreferences sp = context.getSharedPreferences(CONCISE_SETTING, 0);
		sp.edit().putInt(ASC_OR_DESC, mode).commit();
	}

}
