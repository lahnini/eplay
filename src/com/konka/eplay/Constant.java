package com.konka.eplay;

import java.io.Serializable;

/**
 *
 * Created on: 2013-4-8
 *
 * @brief 常量,固化的，程序运行过程中不能修改的
 * @author Eric Fung
 * @date Latest modified on: 2013-4-8
 * @version V1.0.00
 *
 */
public abstract class Constant {
	/** intent中用到的键值对的key部分 */
	public static final String INTENT_KEY_KEYNAME = "keyname";
	/** intent中消息的key */
	public static final String INTENT_KEY_MESSAGE = "Message";

	public static final int MSG_LOST_FOCUS = 1;
	public static final int MSG_GAIN_FOCUS = 2;
	public static final int MSG_DATA_SOURCE_CHANGED = 3;

	public static final int MSG_DOWNLOAD = 1003;
	public static final int MSG_UPLOAD = 1004;
	public static final int MSG_LIST = 1005;
	public static final int MSG_DELETE = 1006;
	public static final int MSG_MKDIR = 1007;
	public static final int MSG_THUMB = 1008;
	public static final int MSG_MOVE = 1009;
	public static final int MSG_QUOTA = 1010;
	public static final int MSG_DOWNLOAD_URL = 1011;
	public static final int MSG_LIST_SPECIFIC_MEDIATYPE = 1012;
	public static final int MSG_RENAME = 1013;
	public static final int MSG_FILE_ATTR = 1014;
	public static final int MSG_CLOUD_ACCOUNT_LOGOUT = 1017;
	// 列出特定类型文件，耗时过多的消息
	public static final int MSG_LIST_SPECIFIC_MEDIATYPE_MORE = 1018;

	/** 用于局域网，从1300到1400 */
	public static final int MSG_HOSTLIST = 1300;
	public static final int MSG_SEARCH_HOST_FINISHED = 1301;
	public static final int SEARCH_HOSTNAME_FINISHED = 1302;
	public static final int SEARCH_FOLDER_SUCCESS_ = 1303;
	public static final int SEARCH_FOLDER_FAIL = 1304;
	public static final int MOUNT_DEVICE_SUCCESS = 1305;
	public static final int MOUNT_DEVICE_FAIL = 1306;
	public static final int UNMOUNT_DEVICE_SUCCESS = 1307;
	public static final int UNMOUNT_DEVICE_FAIL = 1308;
	public static final int HOST_POSITION_CHANGED = 1309;
	public static final int MSG_SEARCH_HOST_ONE_FINISHED = 1310;
	public static final int MSG_LOGON_FAILURE = 1311;
	public static final int SEARCH_FOLDER_SUCCESS_NO_FILE = 1312;

	//用于表明GridView是上滚
	public static final int SCROLL_UP = 1;
	//用于表明GridView是下滚
	public static final int SCROLL_DOWN = 2;

	public static final String LAN_ROOT_PATH = "/mnt/samba";
	public static final String HOST_POSITION = "host_pos";
	public static final String HOST_IP = "host_ip";
	public static final String HOST_NAME = "host_name";

	// 云端根路径
	public static final String CLOUD_ROOT_PATH = "/";
	public static final String RECYCLE = "$RECYCLE.BIN";
	public static final String RECYCLE2 = "Recycled";
	public static final String LOST_DIR = "LOST.DIR";
	// 应用文件夹
	public static final String APP_DIR = "kk.com.konka.eplay";
	public static final String DB_DIR = APP_DIR;

	// 缩略图宽高 kk.com.konka.eplay
	public static final int THUMBNAIL_WIDTH_DP = 226;
	public static final int THUMBNAIL_HEIGHT_DP = 173;

	// 启动播放器传递参数的KEY值
	public static final String PLAY_INDEX = "index";
	public static final String PLAY_PATHS = "paths";
	public static final String OPEN_PATH = "open_path";
	//打开音乐分类浏览
	public static final String SINGERNAME="singerName";
	public static final String SPECIALNAME="singerName";

	// 标签启动
	public static final String LABEL = "label";
	public static final String COLOR_INDEX = "color_index";

	//删除操作
	public static final String DELETEPHOTO = "com.konka.eplay.action.DELETE_PHOTO";
	public static final String DELETEMUSIC = "com.konka.eplay.action.DELETE_MUSIC";
	public static final String DELETEMOVIE = "com.konka.eplay.action.DELETE_MOVIE";

	// 新建新的键代表从文件浏览器传入

	public static final String MAIN_ACTIVITY = "android.intent.action.MAIN";
	public static final String PLAY_PATHS_FROM_BROWSE = "paths_from_browse";
	public static final String PLAY_MUSIC_ACTION = "com.konka.eplay.action.PLAY_MUSIC";
	public static final String PLAY_IMAGE_ACTION = "com.konka.eplay.action.PLAY_IMAGE";
	public static final String PLAY_VIDEO_ACTION = "com.konka.eplay.action.PLAY_MOVIE";
	public static final String MUSIC_INFO = "com.konka.eplay.action.MUSIC_INFO";
	public static final String MOVIE_INFO = "com.konka.eplay.action.MOVIE_INFO";
	public static final String SCAN_BY_SINGER = "com.konka.eplay.action.SCAN_BY_SINGER";
	public static final String SCAN_BY_SPECIAL = "com.konka.eplay.action.SCAN_BY_SPECIAL";

	public static final String HOST_CONNECT_ACTION = "com.konka.eplay.action.HOST_CONNECT";
	public static final String HOST_ISCONNECT = "HOSTISCONNECT";
	public static final String HOSTCONNECT_SEVICE_ACTION = "com.konka.eplay.HostConnectService";

	// 918平台进入快速待机前的广播
	public static final String QUICK_STANDBY_ACTION = "com.konka.enter.fake.standby";
	// EPG预约时间到的切通道广播
	public static final String EPG_ACTION_1 = "com.iflytek.itvs.changechannel";
	public static final String EPG_ACTION_2 = "com.konka.tvsettings.action.EPG_COUNTDOWN";

	//音乐播放状态
	public static final int MUSIC_SERVICE_FLAG_CHANGE_SONG = 21;
	public static final int MUSIC_SERVICE_FLAG_SONG_PAUSE = 22;
	public static final int MUSIC_SERVICE_FLAG_SONG_PLAY = 23;
	public static final int MUSIC_SERVICE_FLAG_SONG_STOP = 24;
	//呼吧启动了某个app
	public static final String START_APP = "Huba_start_app";

	/** 本项目内部的消息 */
	public static enum MessageType implements Serializable {
		MSG_INVALID("无效的消息"), MSG_BOOT("系统启动"), MSG_CUSTOM("自定义的消息按照这种方式添加");

		/** 对消息的注解内容 */
		private String str;

		private MessageType(String paramStr) {
			this.str = paramStr;
		}

		@Override
		public String toString() {
			return this.str;
		}

	}

	/** 文件类型 */
	public static enum MultimediaType {
		/** 非文件(比如以.开头的) */
		MMT_NONE("非文件"),
		/** 图片 */
		MMT_PHOTO("图片"),
		/** 音频 */
		MMT_MUSIC("音频"),
		/** 视频 */
		MMT_MOVIE("视频"),
		/** 文档 */
		MMT_DOCUMENT("文档"),
		/** 安装包 */
		MMT_APK("安装包"),
		/** 压缩包 */
		MMT_ARCHIVE("压缩包"),
		/** 其他格式文件 */
		MMT_OTHER("其他"),
		/** 文件夹 */
		MMT_FOLDER("文件夹"),
		/**标记为红的图片*/
		MMT_REDPHOTO("文件夹"),
		/**标记为蓝的图片*/
		MMT_BLUEPHOTO("文件夹"),
		/**标记为黄的图片*/
		MMT_YELLOWPHOTO("文件夹"),
		/**标记为喜欢的音乐*/
		MMT_LIKEMUSIC("文件夹"),
		/**所有音乐文件*/
		MMT_ALLMUSIC("文件夹") ;


		private String str;

		private MultimediaType(String str) {
			this.str = str;
		}

		public String toString() {
			return this.str;
		}
	}

	public static enum ShareAdress {
		SA_MY_CLOUD("我的云端"), SA_JINSHAN("金山快盘"), SA_HUAWEI("华为网盘"), SA_BAIDU(
						"百度云"), SA_XINLANG("新浪微博"), SA_YOUKU("优酷土豆");
		private String str;

		private ShareAdress(String str) {
			this.str = str;
		}

		public String toString() {
			return this.str;
		}

	}

	/** 文件状态 */
	public static enum FileStatus {
		/** 正常 */
		FS_NORMAL("正常"),
		/** 已选择 */
		FS_SELECTED("已选择"),
		/** 复制 */
		FS_COPY("复制"),
		/** 剪切 */
		FS_CUT("剪切"),
		/** 播放中 */
		FS_PLAYING("播放中"),
		/** 上传中 */
		FS_UPLOADING("上传中"),
		/** 下载中 */
		FS_DOWNLOADING("下载中"), ;

		private String str;

		private FileStatus(String str) {
			this.str = str;
		}

		public String toString() {
			return this.str;
		}
	}

	/** 排序类型 */
	public static enum SortType {
		/** 按时间排序 */
		ST_BY_TIME("time"),
		/** 按名称排序 */
		ST_BY_NAME("name"),
		/**音乐排序方式*/
		ST_BY_SINGER("singer"),
		ST_BY_SPECIAL("special"),
		ST_BY_LIKE("like");


		private String str;

		private SortType(String str) {
			this.str = str;
		}

		public String toString() {
			return this.str;
		}
	}

	/** 排序方式 */
//	public static enum OrderType {
//		/** 降序 */
//		OT_DESC("desc"),
//		/** 升序 */
//		OT_ASC("asc"), ;
//
//		private String str;
//
//		private OrderType(String str) {
//			this.str = str;
//		}
//
//		public String toString() {
//			return this.str;
//		}
//	}

	/** 文件布局方式 */
	public static enum BrowseType {
		/** 缩略图 */
		BT_THUMBNAIL("缩略图"),
		/** 列表 */
		BT_LIST("列表"), ;

		private String str;

		private BrowseType(String str) {
			this.str = str;
		}

		public String toString() {
			return this.str;
		}
	}

	/** 播放模式 */
	public static enum PlayModeType implements Serializable {
		/** 单曲播放 */
		REPEAT,
		/** 顺序循环播放 */
		LOOP,
		/** 随机播放 */
		SHUFFLE,
		/** 顺序播放 */
		IN_ORDER
	}

	/** 数据来源 */
	public static enum DataSourceType {
		/** 本地 */
		DST_LOCAL,
		/** 云端 */
		DST_CLOUD,
		/** 局域网 */
		DST_LAN
	}

	/**
	 * 所有Fragment的table类型
	 */
	public static enum TAB_TYPE {
		// 所有文件fragment
		TAB_ALL,
		// 图片fragment
		TAB_IMG,
		// 视频fragment
		TAB_VIDEO,
		// 音乐fragment
		TAB_MUSIC,
		// 收藏fragment
		TAB_FAV,
		// 上传下载
		TAB_LOADER,
	}

	/** 对数据的操作类型 */
	public static enum OperationType {
		/** 创建，参数为CreateParam */
		OT_CREATE("创建"),
		/** 删除，参数为DeleteParam */
		OT_DELETE("删除"),
		/** 比较 */
		OT_DIFF("比较"),
		/** 重命名，参数为RenameParam */
		OT_RENAME("重命名"),
		/** 移动，参数为MoveParam */
		OT_MOVE("移动"),
		/** 复制，参数为CopyParam */
		OT_COPY("复制"),
		/** 分享 */
		OT_SHARE("分享"),
		/** 播放 */
		OT_PLAY("播放"),
		/** 修改 */
		OT_MODIFY("修改"),
		/** 上传 */
		OT_UPLOAD("上传"),
		/** 下载 ，参数为DownloadParam */
		OT_DOWNLOAD("下载"),
		/** 列出内容 ，参数为ListParam */
		OT_LIST("列出内容"),
		/** 搜索，参数为SearchParam */
		OT_SEARCH("搜索"),
		/** 获取缩略图，参数为ThimbnailParam */
		OT_THMBNAIL("获取缩略图"),
		/** 获取元信息，类似属性，参数为MetaParam */
		OT_META("获取元信息"),
		/** 获取网盘配额信息 */
		OT_QUOTA("网盘配额信息"),
		/** 获取指定多媒体 */
		OT_SPMEDIA("指定多媒体文件"),
		/** 展示文件夹时，打开文件夹 */
		OT_OPEN("打开文件夹"),
		/** 选择文件或文件夹 */
		OT_SELECT("选择文件或文件夹"),
		/** 开始拼图 */
		OT_PUZZLE("拼图"), OT_COLLECT("收藏"), ;

		private String str;

		private OperationType(String str) {
			this.str = str;
		}

		public String toString() {
			return this.str;
		}
	}

	/**
	 * list文件时的过滤类型
	 */
	public static enum LIST_TYPE {
		FILE_ONLY, // 只列出子文件
		FOLDER_ONLY, // 只列出子文件夹
		ALL // 列出子文件和子文件夹
	}

	/**
	 * 云端账号类型
	 */
	public static enum CLOUD_ACCOUNT_TYPE {
		KK_ACCOUNT, // 康佳通行证账号
		HW_ACCOUNT, // 华为云账号
	}

	/**
	 * 云端缩略图尺寸
	 */
	public static enum CLOUD_THUMB_SIZE {
		THUMB_360_240, THUMB_1920_1080, THUMB_640_480, THUMB_1024_768,
	}
}
