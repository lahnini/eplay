package com.konka.eplay;

import iapp.eric.utils.base.Trace;

import java.util.Set;

import com.konka.eplay.Constant.MultimediaType;
import com.konka.eplay.Constant.SortType;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 
 * Created on: 2013-4-8
 * 
 * @brief 配置信息，程序运行过程中允许修改的 使用前请先调用init初始化。在Application的onCreate中调用比较省事。
 * @author Eric Fung
 * @date Latest modified on: 2013-10-9
 * @version V1.0.00
 * 
 */
public class Configuration {
	public static final int INVALID_INT = -1;
	public static final boolean INVALID_BOOLEAN = true;
	public static final float INVALID_FLOAT = -1.0f;
	public static final long INVALID_LONG = -1;
	
	//视频是否是采用硬解播放器
	public static final boolean IS_HWDECODER = true;
	
	// 当前进入的模块是视频还是音乐或图片
	public static MultimediaType curMediaType=MultimediaType.MMT_PHOTO;
	public static SortType sortType = SortType.ST_BY_TIME;
	//是否以文件夹形式浏览音乐
	public static boolean SCAN_BY_FOLDER = true;
	//是否进入图片标签
	public static boolean MARKISENTER = false;
	
	/**是否通过快捷键进入图片视频*/
	public static boolean ISQUICKENTER = false;
	/**是否删除音乐文件*/
	public static boolean ISMUSICDELETED= false;
	public static SortType musicSortType = SortType.ST_BY_NAME;
	public static boolean isStopWrite =false;
	private static Context mContext = null;
	public static boolean isHome=false;
	/** 整个应用通用的SharedPreference */
	private static final String DEFAULT_FILE = "custom";

	private static SharedPreferences getSP(String fileName, Context context) {
		if (null == fileName) {
			return context.getSharedPreferences(DEFAULT_FILE,
							Context.MODE_PRIVATE);
		} else {
			return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		}
	}

	/**
	 * @brief 初始化
	 * @param context
	 *            上下文
	 */
	public static void init(Context context) {
		if (null == context) {
			try {
				throw new Exception("context is null");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mContext = context.getApplicationContext();
		Trace.Info("Configuration is standby.");
	}

	/**
	 * @brief 保存String类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @param value
	 */
	public static void save(String fileName, String key, String value) {
		getSP(fileName, mContext).edit().putString(key, value).commit();
	}

	/**
	 * @brief 保存int类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @param value
	 */
	public static void save(String fileName, String key, int value) {
		getSP(fileName, mContext).edit().putInt(key, value).commit();
	}

	/**
	 * @brief 保存boolean类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @param value
	 */
	public static void save(String fileName, String key, boolean value) {
		getSP(fileName, mContext).edit().putBoolean(key, value).commit();
	}

	/**
	 * @brief 保存float类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @param value
	 */
	public static void save(String fileName, String key, float value) {
		getSP(fileName, mContext).edit().putFloat(key, value).commit();
	}

	/**
	 * @brief 保存long类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @param value
	 */
	public static void save(String fileName, String key, long value) {
		getSP(fileName, mContext).edit().putLong(key, value).commit();
	}

	/**
	 * @brief 保存Set<String>类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @param value
	 */
	public static void save(String fileName, String key, Set<String> value) {
		getSP(fileName, mContext).edit().putStringSet(key, value).commit();
	}

	/**
	 * @brief 获取String类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @return key不存在时返回null
	 */
	public static String getString(String fileName, String key) {
		return getSP(fileName, mContext).getString(key, null);
	}

	/**
	 * @brief 获取int类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @return key不存在时返回INVALID_INT
	 */
	public static int getInt(String fileName, String key) {
		return getSP(fileName, mContext).getInt(key, INVALID_INT);
	}

	/**
	 * @brief 获取boolean类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @return key不存在时返回INVALID_BOOLEAN
	 */
	public static boolean getBoolean(String fileName, String key) {
		return getSP(fileName, mContext).getBoolean(key, INVALID_BOOLEAN);
	}

	/**
	 * @brief 获取float类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @return key不存在时返回INVALID_FLOAT
	 */
	public static float getFloat(String fileName, String key) {
		return getSP(fileName, mContext).getFloat(key, INVALID_FLOAT);
	}

	/**
	 * @brief 获取long类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @return key不存在时返回INVALID_LONG
	 */
	public static long getLong(String fileName, String key) {
		return getSP(fileName, mContext).getLong(key, INVALID_LONG);
	}

	/**
	 * @brief 获取Set<String>类型的信息
	 * @param fileName
	 *            需要操作的文件名.若为null，会使用默认文件进行操作
	 * @param key
	 * @return key不存在时返回null
	 */
	public static Set<String> getSet(String fileName, String key) {
		return getSP(fileName, mContext).getStringSet(key, null);
	}
}
