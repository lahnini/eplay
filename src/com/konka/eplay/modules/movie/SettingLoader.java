package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.konka.android.tv.KKPictureManager;

/**
 * 保存设置，读取设置
 * 
 * @author situhui
 * 
 */
public class SettingLoader {

	private Activity mActivity;
	private SharedPreferences mPreferences;

	private static final String BACKLIGHT_KEY = "backlight";
	private static final String PICTURE_MODE_KEY = "picture_mode";
	private static final String LOOP_MODE_KEY = "loop_mode";
	private static final String SCALE_MODE_KEY = "scale_mode";

	public static final String PICTURE_MODE_VIVID = "vivid";
	public static final String PICTURE_MODE_SOFT = "soft";
	public static final String PICTURE_MODE_DYNAMIC = "dynamic";
	public static final String PICTURE_MODE_NORMAL = "normal";

	public SettingLoader(Activity activity) {
		this.mActivity = activity;
		mPreferences = mActivity.getPreferences(Activity.MODE_PRIVATE);
	}

	/**
	 * 设置背光
	 * 
	 * @param value
	 */
	public void setBacklight(int value) {
		if (value < 0) {
			value = 0;
		}
		else if (value > 100) {
			value = 100;
		}
		Trace.Info("#####setBacklight " + value);
		KKPictureManager.getInstance(mActivity).setBacklight((short) value);
	}

	/**
	 * 返回当前亮度值
	 * 
	 * @return
	 */
	public int getCurBacklight() {
		return (int) KKPictureManager.getInstance(mActivity).getBacklight();
	}

	/**
	 * 返回被持久化的activity对应亮度值
	 * 
	 * @return -1表示没有
	 */
	public int getBacklightFromPreferences() {
		return mPreferences.getInt(BACKLIGHT_KEY, -1);

	}

	/**
	 * 持久化activity对应亮度值
	 * 
	 * @param value
	 */
	public void setBacklightToPreferences(int value) {
		if (value < 0) {
			value = 0;
		}
		else if (value > 100) {
			value = 100;
		}
		Editor editor = mPreferences.edit();
		editor.putInt(BACKLIGHT_KEY, value);
		editor.commit();
	}

	/**
	 * 返回存储的图像模式
	 * 
	 * @return
	 */
	public String getPMFromPreferences() {
		return mPreferences.getString(PICTURE_MODE_KEY, PICTURE_MODE_NORMAL);
	}

	/**
	 * 存储当前图像模式
	 * 
	 * @param mode
	 */
	public void setPMToPreferences(String mode) {
		if (mode == null) {
			return;
		}
		if (mode.equals(PICTURE_MODE_DYNAMIC) || mode.equals(PICTURE_MODE_NORMAL) || mode.equals(PICTURE_MODE_SOFT)
				|| mode.equals(PICTURE_MODE_VIVID)) {
			Editor editor = mPreferences.edit();
			editor.putString(PICTURE_MODE_KEY, mode);
			editor.commit();
		}
	}

	/**
	 * 返回存储的屏显模式
	 * 
	 * @return
	 */
	public int getSMFromPreferences() {
		return mPreferences.getInt(SCALE_MODE_KEY, MoviePlayer.SCREEN_FULL);
	}

	/**
	 * 存储当前屏显模式
	 * 
	 * @param mode
	 */
	public void setSMToPreferences(int mode) {
		if (mode == MoviePlayer.SCREEN_DEFAULT || mode == MoviePlayer.SCREEN_FULL
				|| mode == MoviePlayer.SCREEN_FULL_RESIZE) {
			Editor editor = mPreferences.edit();
			editor.putInt(SCALE_MODE_KEY, mode);
			editor.commit();
		}
	}

	/**
	 * 返回存储的循环模式
	 * 
	 * @return
	 */
	public int getLMFromPreferences() {
		return mPreferences.getInt(LOOP_MODE_KEY, MoviePlayer.LOOP_ALL);
	}

	/**
	 * 存储当前循环模式
	 * 
	 * @param mode
	 */
	public void setLMFromPreferences(int mode) {
		if (mode == MoviePlayer.LOOP_ALL || mode == MoviePlayer.LOOP_SHUFFLE || mode == MoviePlayer.LOOP_SINGLE) {
			Editor editor = mPreferences.edit();
			editor.putInt(LOOP_MODE_KEY, mode);
			editor.commit();
		}
	}
}
