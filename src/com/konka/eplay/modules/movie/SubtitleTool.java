package com.konka.eplay.modules.movie;

import java.util.ArrayList;
import java.util.List;

public class SubtitleTool {

	private String mVideoPath = "";
	private String mSubtitlePath = "";
	private List<String> mSubtitlePaths = new ArrayList<String>();

	public SubtitleTool(String path) {
		mVideoPath = path;
		discussSubtitlePathAndType();
	}

	/**
	 * 依据播放的视频获取视频字幕的全路径和字幕类型
	 */
	private List<String> discussSubtitlePathAndType() {
		// 如果不存在后缀，直接返回空字串
		if (mVideoPath.lastIndexOf(".") == -1) {
			return mSubtitlePaths;
		}

		String titlePath = mVideoPath.substring(0, mVideoPath.lastIndexOf("."));

		// 文本srt格式
		mSubtitlePath = titlePath + ".srt";
		if (Tools.isFileExist(mSubtitlePath)) {
			mSubtitlePaths.add(mSubtitlePath);
		}

		// 文本ssa格式
		mSubtitlePath = titlePath + ".ssa";
		if (Tools.isFileExist(mSubtitlePath)) {
			mSubtitlePaths.add(mSubtitlePath);
		}

		// 文本ass格式
		mSubtitlePath = titlePath + ".ass";
		if (Tools.isFileExist(mSubtitlePath)) {
			mSubtitlePaths.add(mSubtitlePath);
		}

		// 文本smi格式
		mSubtitlePath = titlePath + ".smi";
		if (Tools.isFileExist(mSubtitlePath)) {
			mSubtitlePaths.add(mSubtitlePath);
		}

		// 文本txt格式
		mSubtitlePath = titlePath + ".txt";
		if (Tools.isFileExist(mSubtitlePath)) {
			mSubtitlePaths.add(mSubtitlePath);
		}

		// image格式字幕
		mSubtitlePath = titlePath + ".idx";
		String tempImagePath = titlePath + ".sub";
		if (Tools.isFileExist(mSubtitlePath)
						&& Tools.isFileExist(tempImagePath)) {
			mSubtitlePaths.add(mSubtitlePath);
		}
		return mSubtitlePaths;
	}

	public List<String> getSubtitlePaths() {
		return mSubtitlePaths;
	}

}
