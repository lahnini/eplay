package com.konka.eplay.modules.movie;

import iapp.eric.utils.base.Trace;

import java.io.File;

import com.konka.eplay.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 操作播放记录
 * 
 * @author situ hui
 * 
 */
public class PlayRecordHelper {
	public static final String PLAY_RECORD_PREFERENCES = "play_record";
	private static final String TAG_OFFSET = "offset";
	private static final String TAG_COMPLETED = "completed";
	private SharedPreferences mPlayRecordPreferences;

	public PlayRecordHelper(Context context) {
		mPlayRecordPreferences = context.getSharedPreferences(PLAY_RECORD_PREFERENCES, Context.MODE_PRIVATE);
	}

	/**
	 * 获取路径视频已播放时间
	 * 
	 * @param path
	 * @return 如果没有之前没有存储，返回0
	 */
	public int getPlayedOffset(String path) {
		String subPath = Utils.getSubPath(path);
		return mPlayRecordPreferences.getInt(subPath + getFileMd5(path) + TAG_OFFSET, 0);
	}

	/**
	 * 获取路径视频是否已播完
	 * 
	 * @param path
	 * @return
	 */
	public boolean isPlayCompleted(String path) {
		String subPath = Utils.getSubPath(path);
		return mPlayRecordPreferences.getBoolean(subPath + getFileMd5(path) + TAG_COMPLETED, false);
	}

	/**
	 * 存储路径视频已播放时间
	 * 
	 * @param path
	 * @param offset
	 */
	public void setPlayerOffset(String path, int offset) {
		Editor editor = mPlayRecordPreferences.edit();
		if (offset < 0) {
			offset = 0;
		}
		String subPath = Utils.getSubPath(path);
		editor.putInt(subPath + getFileMd5(path) + TAG_OFFSET, offset);
		editor.commit();
	}

	/**
	 * 记录路径视频已播完
	 * 
	 * @param path
	 */
	public void setPlayCompleted(String path) {
		Editor editor = mPlayRecordPreferences.edit();
		String subPath = Utils.getSubPath(path);
		editor.putBoolean(subPath + getFileMd5(path) + TAG_COMPLETED, true);
		editor.commit();
	}
	
	public void clearPlayRecord() {
		Editor editor = mPlayRecordPreferences.edit();
		editor.clear();
		editor.commit();
	}

	private static final int FILE_MD5_BYTES = 256;

	private String getFileMd5(String path) {
		long start = System.currentTimeMillis();
		File file = new File(path);
		String md5 = Utils.Md5(file, FILE_MD5_BYTES);
		long end = System.currentTimeMillis();
		Trace.Debug("get file md5 time :" + (end - start));
		return md5;
	}
}
