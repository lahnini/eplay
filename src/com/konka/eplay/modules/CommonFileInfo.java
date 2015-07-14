package com.konka.eplay.modules;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

import com.konka.eplay.R.string;
import com.konka.eplay.Utils;
import com.konka.eplay.Constant.MultimediaType;

/**
 *
 * Created on: 2013-10-9
 *
 * @brief 文件信息
 * @author Eric Fung
 * @date Latest modified on: 2013-10-9
 * @version V1.0.00
 *
 */
public class CommonFileInfo implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

	/** 名称 */
	private String name;
	/**名称首字母*/
	private String firstLetter;
	/**名字拼音*/
	private String fullPinyin;
	/**专辑拼音*/
	private String specialPinyin;
	/**歌手拼音*/
	private String singerPinyin;
	/** 全路�? */
	private String path;
	//private String parentPath;
	/** 大小 */
	private long size;
	/** 上次修改时间 */
	private Date modifiedTime;
	/** 创建时间 */
	private Date createdTime;
	/** 文件或目录的md5�? */
	private String md5;

	/** 是否包含子目�? */
	private boolean hasSubFolder;
	/** 是否是文件夹 */
	private boolean isDir;
	/** 如果是文件夹，则表示里面有多少文件，否则此变量无意义 */
	private int moviecount;
	private int musiccount;
	private int photocount;
	private int count = 0;
	/**图片标签*/
	private boolean isRed = false;
	private boolean isBlue = false;
	private boolean isYellow = false;
	/**音乐标记为喜欢*/
	private boolean isLike=false;

	/** add by lhq 歌手 */
	public String singer = "";
	/** add by lhq 专辑 */
	public String special = "";
	/** add by lhq 标题 */
	public String title = "";
	/** add by xuyunu 时间 */
	public String time = "";
	/** add by xuyunu mp3比特率 */
	public int bitrate = 0;

	/**add by mcsheng 最近播放时间*/
	public long recentPlayTime = 0;

	/** 类型 */
	public MultimediaType type = MultimediaType.MMT_NONE;

	public CommonFileInfo() {

	}

	public CommonFileInfo(String name, String path, long size,
					Date modifiedTime, Date createdTime, String md5,
					boolean hasSubFolder, boolean isDir, int count,
					MultimediaType type ) {
		super();
		this.name = name;
		this.path = path;
		this.size = size;
		this.modifiedTime = modifiedTime;
		this.createdTime = createdTime;
		this.md5 = md5;
		this.hasSubFolder = hasSubFolder;
		this.isDir = isDir;
		this.count = count;
		this.type = type;
	}

	/**
	 *
	 * @param file
	 *            java.io.File
	 */
	public CommonFileInfo(File file) {
		this.md5 = "";
		this.name = file.getName();
		this.path = file.getPath();
		this.size = file.length();
		this.isDir = file.isDirectory();
		this.modifiedTime = new Date(file.lastModified());
		this.createdTime = modifiedTime; // 暂时也设置为�?后修改时�?
		if (this.isDir) {
			File[] subs = file.listFiles();
			if (subs != null) {
				int countmovie = 0;
				int countmusic = 0;
				int countphoto = 0;
				for (int i = 0; i < subs.length; i++) {
					if (subs[i].isFile()) {
						if (MultimediaType.MMT_MOVIE == Utils.getMmt(subs[i]
										.getAbsolutePath())) {
							countmovie++;
						} else if (MultimediaType.MMT_MUSIC == Utils
										.getMmt(subs[i].getAbsolutePath())) {
							countmusic++;
						} else if (MultimediaType.MMT_PHOTO == Utils
										.getMmt(subs[i].getAbsolutePath())) {
							countphoto++;
						} else {
							continue;
						}
					}
					this.moviecount = countmovie;
					this.musiccount = countmusic;
					this.photocount = countphoto;
					this.count = subs.length;
				}
			}
		}
	}


	public String getPath() {
		return this.path;
	}

	public String getParentPath() {
		return Utils.getParentPath(path);
	}

	public void setPath(String path) {
		this.path = path;
	}


	public String getName() {
		if (name == null) {
			if (path != null) {
				return Utils.getFileName(path);
			}
		}
		return this.name;
	}
	public String getFirstLetter() {
		if(this.firstLetter!=null){
			
			return this.firstLetter;
		}else {
			
		String firstchar=Utils.getFirstChar(this.name);
		setFirstLetter(firstchar);
		return firstchar;
		}
	}
	public void setFirstLetter(String fstletter) {
		this.firstLetter=fstletter;
	}
	public String getFullPinyin() {
		if(this.fullPinyin!=null){
			
			return this.fullPinyin;
		}else {
			
			String firstchar=Utils.getFullPinYin(this.name);
			setFullPinyin(firstchar);
			String firstLetter=firstchar.substring(0,1).toUpperCase();
			if (!firstLetter.matches("[a-zA-Z0-9]+")) 
				firstLetter="#";
			
			setFirstLetter(firstLetter);
			return firstchar;
		}
	}
	public void setFullPinyin(String fstletter) {
		this.fullPinyin=fstletter;
	}
	public String getSpecialFirstLetter() {
		if(this.specialPinyin!=null){
			
			return this.specialPinyin;
		}else {
			
			String firstchar=Utils.getFullPinYin(this.special);
			setSpecialFirstLetter(firstchar);
			return firstchar;
		}
	}
	public void setSpecialFirstLetter(String fstletter) {
		this.specialPinyin=fstletter;
	}
	public String getSingerFirstLetter() {
		if(this.singerPinyin!=null){
			
			return this.singerPinyin;
		}else {
			
			String firstchar=Utils.getFullPinYin(this.singer);
			setSingerFirstLetter(firstchar);
			return firstchar;
		}
	}
	public void setSingerFirstLetter(String fstletter) {
		this.singerPinyin=fstletter;
	}

	public long getSize() {
		return size;
	}

	public Date getCreatedTime() {
		return this.createdTime;
	}

	public Date getModifiedTime() {
		return this.modifiedTime;
	}

	public void setModifiedTime(Date time) {
		this.modifiedTime = time;
	}

	public String getMD5() {
		return this.md5;
	}

	public boolean isDir() {
		return this.isDir;
	}

	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}

	public boolean hasSubFolder() {
		return this.hasSubFolder;
	}

	public void setName(String name) {
		this.name = name;
		Utils.getFullPinYin(name);
		Utils.getFirstChar(name);
	}
	


	public String getSinger() {
		return singer;
	}

	public void setSinger(String singer) {
		this.singer = singer;
	}

	public String getSpecial() {
		return special;
	}

	public void setSpecial(String special) {
		this.special = special;
	}

	public void setChildrenCount(int count) {
		this.count = count;
	}

	public int getChildrenCount() {
		return this.count;
	}

	public int getChildrenPhotoCount() {
		return this.photocount;
	}
	public void setChildrenPhotoCount(int count) {
		this.photocount=count;
	}
	public void setChildrenMusicCount(int count) {
		this.musiccount=count;
	}

	public int getChildrenMusicCount() {
		return this.musiccount;
	}

	public int getChildrenMovieCount() {
		return this.moviecount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getBitrate() {
		return bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	@Override
	public int hashCode() {
		return this.getPath().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof CommonFileInfo) {
			CommonFileInfo other = (CommonFileInfo) o;
			if (this.getPath().equals(other.getPath())) {
				return true;
			}else {
				return false;
			}
		}
		return false;
	}

	public void setIsRed(boolean isred) {
		isRed = isred;
	}

	public void setIsBlue(boolean isblue) {
		isBlue = isblue;
	}

	public void setIsYellow(boolean isyellow) {
		isYellow = isyellow;
	}

	public boolean getIsYellow() {
		return isYellow;
	}

	public boolean getIsBlue() {
		return isBlue;
	}

	public boolean getIsRed() {
		return isRed;
	}
	public boolean getIsLike() {
		return isLike;
	}
	public void setIsLike(boolean islike) {
		isLike=islike;
	}


	/**
	 * 	若this为目录，当有文件删除后调用此方法更新目录下文件数目信息
	 */
	public void updateFileCount() {
		if (this.isDir) {
			File file = new File(this.path);
			File[] subs = file.listFiles();
			if (subs != null) {
				int countmovie = 0;
				int countmusic = 0;
				int countphoto = 0;
				for (int i = 0; i < subs.length; i++) {
					if (subs[i].isFile()) {
						if (MultimediaType.MMT_MOVIE == Utils.getMmt(subs[i]
										.getAbsolutePath())) {
							countmovie++;
						} else if (MultimediaType.MMT_MUSIC == Utils
										.getMmt(subs[i].getAbsolutePath())) {
							countmusic++;
						} else if (MultimediaType.MMT_PHOTO == Utils
										.getMmt(subs[i].getAbsolutePath())) {
							countphoto++;
						} else {
							continue;
						}
					}
				}
				this.moviecount = countmovie;
				this.musiccount = countmusic;
				this.photocount = countphoto;
				this.count = subs.length;
			}
		}
	}
}
