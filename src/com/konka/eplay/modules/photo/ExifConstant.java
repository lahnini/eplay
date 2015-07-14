package com.konka.eplay.modules.photo;

/**
 * Exif常量定义类</br> <b>创建时间：</b>2015-3-18
 * 
 * @author mcsheng
 */
public class ExifConstant {

	/**
	 * 测光模式
	 */
	public class ExposureMeteringMethod {
		/**
		 * 未知
		 */
		public static final int UNKNOWN = 0;
		/**
		 * 平均
		 */
		public static final int AVERAGE = 1;
		/**
		 * 偏中心平均
		 */
		public static final int CENTER_WEIGHTED_AVERAGE = 2;
		/**
		 * 点
		 */
		public static final int SPOT = 3;
		/**
		 * 多点
		 */
		public static final int MULTI_SPOT = 4;
		/**
		 * 多区
		 */
		public static final int MULTI_SEGMENT = 5;
		/**
		 * 部分
		 */
		public static final int PARTIAL = 6;
		/**
		 * 其他
		 */
		public static final int OTHER = 255;
	}

	/**
	 * 闪光灯模式
	 */
	public class FlashMode {
		/**
		 * 无闪光
		 */
		public static final int NO_FLASH = 0;
		/**
		 * 闪光
		 */
		public static final int FIRED = 1;
		/**
		 * 闪光，无选通返回
		 */
		public static final int FIRED_RETURN_NOT_DETECTED = 5;
		/**
		 * 闪光，选通返回
		 */
		public static final int FIRED_RETURN_DETECTED = 7;
		/**
		 * 闪光，强制
		 */
		public static final int ON = 9;
		/**
		 * 闪光，强制，无选通返回
		 */
		public static final int ON_RETURN_NOT_DETECTED = 13;
		/**
		 * 闪光，强制，带选通返回
		 */
		public static final int ON_RETURN_DETECTED = 15;
		/**
		 * 无闪光，强制
		 */
		public static final int OFF = 16;
		/**
		 * 无闪光，自动
		 */
		public static final int AUTO_DID_NOT_FIRE = 24;
		/**
		 * 闪光，自动
		 */
		public static final int AUTO_FIRED = 25;
		/**
		 * 闪光，自动，无选通返回
		 */
		public static final int AUTO_FIRED_RETURN_NOT_DETECTED = 29;
		/**
		 * 闪光，自动，带选通返回
		 */
		public static final int AUTO_FIRED_RETURN_DETECTED = 31;
		/**
		 * 无闪光功能
		 */
		public static final int NO_FLASH_FUNCTION = 32;
		/**
		 * 闪光，红眼
		 */
		public static final int FIRED_RED_EYE_REDUCTION = 65;
		/**
		 * 闪光，红眼，无选通返回
		 */
		public static final int FIRED_RED_EYE_NOT_DETECTED = 69;
		/**
		 * 闪光，红眼，带选通返回
		 */
		public static final int FIRED_RED_EYE_DETECTED = 71;
		/**
		 * 闪光，强制，红眼
		 */
		public static final int ON_RED_EYE_REDUCTION = 73;
		/**
		 * 闪光，强制，红眼，无选通返回
		 */
		public static final int ON_RED_EYE_NOT_DETECTED = 77;
		/**
		 * 闪光，强制，红眼，带选通返回
		 */
		public static final int ON_RED_EYE_DETECTED = 79;
		/**
		 * 闪光，自动，红眼
		 */
		public static final int AUTO_FIRED_RED_EYE_REDUCTION = 89;
		/**
		 * 闪光，自动，无选通返回，红眼
		 */
		public static final int AUTO_FIRED_RED_EYE_NOT_DETECTED = 93;
		/**
		 * 闪光，自动，带选通返回，红眼
		 */
		public static final int AUTO_FIRED_RED_EYE_DETECTED = 95;
	}

	/**
	 * 对比度
	 */
	public class Contrast {
		/**
		 * 正常
		 */
		public static final int NORMAL = 0;
		/**
		 * 柔和
		 */
		public static final int SOFT = 1;
		/**
		 * 强烈
		 */
		public static final int HARD = 2;
	}

	/**
	 * 曝光程序
	 */
	public class ExposureProgram {
		/**
		 * 手动控制
		 */
		public static final int MANUAL_CONTROL = 1;

		/**
		 * 正常
		 */
		public static final int PROGRAM_NORMAL = 2;

		/**
		 * 光圈优先级
		 */
		public static final int APERTURE_PRIORITY = 3;

		/**
		 * 快门优先级
		 */
		public static final int SHUTTER_PRIORITY = 4;

		/**
		 * 创作程序（偏重使用视野深度)
		 */
		public static final int PROGRAM_CREATIVE = 5;

		/**
		 * 操作程序（偏重使用快门速度
		 */
		public static final int PROGRAM_ACTION = 6;

		/**
		 * 纵向模式
		 */
		public static final int PORTRAIT_MODE = 7;

		/**
		 * 横向模式
		 */
		public static final int LANDSCAPE_MODE = 8;

	}

	/**
	 * 饱和度
	 */
	public class Saturation {

		/**
		 * 正常
		 */
		public static final int NORMAL = 0;

		/**
		 * 低饱和度
		 */
		public static final int LOW_SATURATION = 1;

		/**
		 * 高饱和度
		 */
		public static final int HIGH_SATURATION = 2;

	}

	/**
	 * 清晰度
	 */
	public class Sharpness {

		/**
		 * 正常
		 */
		public static final int NORMAL = 0;

		/**
		 * 柔和
		 */
		public static final int SOFT = 1;

		/**
		 * 强烈
		 */
		public static final int HARD = 2;
	}

	/**
	 * 白平衡模式
	 */
	public class WhiteBalanceMode {

		/**
		 * 自动
		 */
		public static final int AUTO_WHITE_BALANCE = 0;

		/**
		 * 手动
		 */
		public static final int MANUAL_WHITE_BALANCE = 1;
	}

}