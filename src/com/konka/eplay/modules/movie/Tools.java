package com.konka.eplay.modules.movie;

import java.io.File;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import android.content.Context;
import android.widget.PopupWindow;

/**
 * 辅助工具类
 * 
 * @author situhui
 * 
 */
public class Tools {
	/**
	 * 将毫秒数格式化为mm:ss
	 * 
	 * @param msec
	 * @return 格式化后的时间字符串
	 */
	public static String formatMsec(int msec) {
		if (msec >= 0) {
			int seconds = msec / 1000;
			if (seconds == 0) {
				return "00:01";
			}
			int min = seconds / 60;
			int sec = seconds % 60;
			if (min >= 100) {
				return String.format("%d:%02d", min, sec);
			}
			else {
				return String.format("%02d:%02d", min, sec);
			}

		}
		return "00:00";
	}

	/**
	 * 将毫秒数格式化为mm:ss
	 * 
	 * @param msec
	 * @return 格式化后的时间字符串
	 */
	public static String formatCurrMsec(int msec) {
		if (msec >= 0) {
			int seconds = msec / 1000;
			if (seconds == 0) {
				return "00:00";// 与formatMsec方法不同处
			}
			int min = seconds / 60;
			int sec = seconds % 60;
			if (min >= 100) {
				return String.format("%d:%02d", min, sec);
			}
			else {
				return String.format("%02d:%02d", min, sec);
			}

		}
		return "00:00";
	}

	/**
	 * @param bytes
	 * @return
	 */
	public static String formatSize(long bytes) {
		long kbytes = bytes / 1024;
		if (kbytes < 1) {
			return "1K";
		}
		else if (kbytes < 1024) {
			return kbytes + "K";
		}
		else if (kbytes >= 1024) {
			long mbytes = kbytes / 1024;
			if (mbytes < 1) {
				return "1M";
			}
			else if (mbytes < 1024) {
				return mbytes + "M";
			}
			else if (mbytes >= 1024) {
				double gbytes = mbytes / 1024.0;
				DecimalFormat format = new DecimalFormat("#.00");
				format.setRoundingMode(RoundingMode.HALF_UP);
				String size = format.format(gbytes);
				return size + "G";
			}
		}
		return "";
	}

	/**
	 * dip转px
	 * 
	 * @param context
	 * @param dip
	 * @return 单位为px的长度
	 */
	public static int dip2px(Context context, int dip) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (dip * density + 0.5f);
	}

	/**
	 * 判断文件是否存在.
	 */
	public static boolean isFileExist(File file) {
		return file.exists();
	}

	/**
	 * 判断文件是否存在.
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isFileExist(String path) {
		return isFileExist(new File(path));
	}

	/**
	 * 获取给定目录下文件与文件夹的总数
	 * 
	 * @param path
	 *            给定目录的绝对路径
	 * @return 返回给给定目录下文件与文件夹的总数，如果参数path为null，结果返回-1，如果参数path不是目录，结果返回-2。
	 */
	public static int getFileAmount(String path) {
		if (path == null) {
			return -1;
		}
		File file = new File(path);
		if (!file.isDirectory()) {
			return -2;
		}
		return file.listFiles().length;
	}

	/**
	 * 删除给定路径目录下的文件与子目录，如果参数path为null，do nothing。
	 * 
	 * @param path
	 *            包含要删除文件的目录的路径
	 */
	public static void deleteFiles(String path) {
		if (path != null) {
			File dir = new File(path);
			if (dir.isDirectory()) {
				for (File file : dir.listFiles()) {
					file.delete();
				}
			}
		}
	}

	/**
	 * Set whether this window is touch modal or if outside touches will be sent
	 * to other windows behind it.
	 * 
	 */
	public static void setPopupWindowTouchModal(PopupWindow popupWindow, boolean touchModal) {
		if (null == popupWindow) {
			return;
		}
		Method method;
		try {

			method = PopupWindow.class.getDeclaredMethod("setTouchModal", boolean.class);
			method.setAccessible(true);
			method.invoke(popupWindow, touchModal);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
