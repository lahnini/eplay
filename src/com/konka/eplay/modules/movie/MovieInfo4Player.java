package com.konka.eplay.modules.movie;

/**
 * 播放器使用到的视频相关信息
 * 
 * @author situhui
 * 
 */
public class MovieInfo4Player {
	private String mPath;
	private String mName;

	public MovieInfo4Player(String path) {
		this.mPath = path;
	}

	public String getPath() {
		return mPath;
	}

	public String getName() {
		if (mName != null) {
			return mName;
		}
		if (mPath != null) {
			int begin = mPath.lastIndexOf("/") + 1;
			int end = mPath.lastIndexOf(".");
			if (end < mPath.length() && begin < end) {
				mName = mPath.substring(begin, end);
			}
		}
		return mName;
	}

}
